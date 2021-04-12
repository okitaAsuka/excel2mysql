package org.asuka.export.read

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.asuka.export.dao.CoreDao
import org.asuka.export.util.ExcelUtil
import org.asuka.export.util.StringUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import kotlin.system.exitProcess

/**
 * @author: orange
 * @Description: excel读取类
 * @Date: Create in 2018.04.04
 */

@Component
class ExcelReader {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var coreDao: CoreDao

    private var workbook: XSSFWorkbook? = null

    @Value("\${docPath}")
    lateinit var docPath: String

    @Value("\${fileType}")
    lateinit var fileType: String

    @Value("\${exportType}")
    var exportType: Int = 0

    @Value("\${mysql.tablePrefix}")
    lateinit var tablePrefix: String

    @Value("\${mysql.createInfoRowNum}")
    var createInfoRowNum: Int = 0

    @Value("\${mysql.dropTableBeforeCreate}")
    var dropTableBeforeCreate: Boolean = false

    @Value("\${json.exportPath}")
    lateinit var exportPath: String

    val drop: String = "drop table IF EXISTS "

    val create: String = "create table IF NOT EXISTS "

    val insert: String = "insert into "

    /**
     * create table
     */
    private fun createTable(sheet: Sheet, tableName: String): String {

        var sql = StringBuffer()

        sql.append(create)
        sql.append("$tableName ( ")

        // 从指定行中读取到字段信息
        val dbRow: Row = sheet.getRow(createInfoRowNum)

        var fields = StringBuffer()

        // 拼接创表语句
        dbRow.forEach {

            val value = ExcelUtil.getCellData(it)
            if (value > "") {

                val data = value.split(";")
                sql.append("${data[0]} ").append(data[1]).append(" NOT NULL")
                        .append(" COMMENT ").append("'${ExcelUtil.getAssignRowCellData(sheet, it, 0)}'")
                        .append(",")

                fields.append(data[0]).append(",")
            }
        }

        sql = StringBuffer(sql.removeRange(IntRange(sql.length - 1, sql.length - 1)))
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;")

        if (dropTableBeforeCreate)
            coreDao.sqlExecute("$drop $tableName;")

        coreDao.sqlExecute(sql.toString())

        fields = StringBuffer(fields.removeRange(IntRange(fields.length - 1, fields.length - 1)))
        return fields.toString()
    }

    private fun isEmptyRow(dataRow: Row) : Boolean {

        // 如果首列ID列为空则跳过
        val firstRow = dataRow.getCell(0)
        if (firstRow == null || firstRow.cellType == CellType.BLANK)
            return true

        val idRowValue = ExcelUtil.getCellData(firstRow)
        if (idRowValue.isBlank())
            return true

        return false
    }

    fun readCellData(dataRow: Row, sheet: Sheet, dataSB: StringBuilder, fieldByMap:  MutableMap<Int, String>?) {

        for (cellIndex in 0..dataRow.lastCellNum) {
            if (ExcelUtil.needReadData(sheet, cellIndex, createInfoRowNum)) {

                val row = dataRow.getCell(cellIndex)
                var data: String
                if (row != null) {

                    data = ExcelUtil.getCellData(row)
                    if (!StringUtil.isNumber(data))
                        data = "\"$data\""
                } else // 处理空字符串
                    data = "\"\""

                if (exportType == 1) {
                    dataSB.append("$data,")
                } else {
                    dataSB.append("\"${fieldByMap!![cellIndex]}\"").append(":").append(data).append(",")
                }
            }
        }
    }

    fun writeToMysql(sheet: Sheet, tableName: String, fields: String) {

        logger.info("准备读取数据并写入$tableName, 共有${sheet.lastRowNum}行")

        var insertSql = StringBuilder(insert + tableName + " (${fields}) values ")

        // 从第x行中读取到创库信息
        for (index in (createInfoRowNum + 1)..sheet.lastRowNum) {

            val dataRow = sheet.getRow(index) ?: continue
            if (isEmptyRow(dataRow))
                continue

            insertSql.append("(")
            var dataSB = StringBuilder("")

            readCellData(dataRow, sheet, dataSB, null)

            dataSB = StringBuilder(dataSB.removeRange(IntRange(dataSB.length - 1, dataSB.length - 1)))
            insertSql.append(dataSB.toString())
            insertSql.append("),")
        }

        insertSql = StringBuilder(insertSql.removeRange(IntRange(insertSql.length - 1, insertSql.length - 1)))
        insertSql.append(";")

        coreDao.sqlExecute(insertSql.toString())
    }

    fun writeToJsonFile(sheet: Sheet, tableName: String) {

        // 索引字段名
        val dbRow: Row = sheet.getRow(createInfoRowNum)
        var fieldByMap = mutableMapOf<Int, String>()
        dbRow.forEach {

            val value = ExcelUtil.getCellData(it)
            if (value > "") {
                val fieldInfo = value.split(";")
                fieldByMap[it.columnIndex] = fieldInfo[0]
            }
        }

        val dataSB = StringBuilder("[")
        for (index in (createInfoRowNum + 1)..sheet.lastRowNum) {

            val dataRow = sheet.getRow(index) ?: continue
            if (isEmptyRow(dataRow))
                continue

            dataSB.append("{")

            readCellData(dataRow, sheet, dataSB, fieldByMap)
            dataSB.delete(dataSB.length - 1, dataSB.length)
            dataSB.append("}")

            if (index != sheet.lastRowNum)
                dataSB.append(",")
        }

        dataSB.append("]")

        val dic = File(exportPath)
        if (! dic.exists())
            dic.mkdir()

        val fullPath = "$exportPath$tableName.json"
        val file = File(fullPath)
        if (file.exists())
            file.delete()
        file.writeText(dataSB.toString())
    }

    /**
     * 处理文件
     */
    fun dealFile(fileName: String) {

        workbook?.let {
            it.sheetIterator().forEach { sheet ->

                val sheetName = sheet.sheetName
                // 未命名的sheet不读

                if (sheetName.indexOf("Sheet") < 0) {

                    logger.info("读取页签$${sheetName}")
                    logger.info("共有${sheet.lastRowNum + 1}行数据")

                    val saveName = tablePrefix + sheetName

                    if (exportType == 1) {

                        // 拼接出字段列
                        val fields = createTable(sheet, saveName)

                        writeToMysql(sheet, saveName, fields)
                    } else {
                        writeToJsonFile(sheet, saveName)
                    }
                }
            }
        }

    }

    fun readFile(_path: String) {

        val path = docPath + "\\" + _path + fileType
        val wb: XSSFWorkbook?
        val checkFile = File(path)

        if (checkFile.exists()) {

            val pkg = OPCPackage.open(File(path))
            wb = XSSFWorkbook(pkg)
            this.workbook = wb
            logger.info("读取文件$path")

            pkg.close()
        } else {
            logger.error("file not found:$path")
            exitProcess(0)
        }
    }

}