package org.asuka.export.read

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.asuka.export.ReadModel
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

    @Value("\${tablePrefix}")
    lateinit var tablePrefix: String

    @Value("\${createInfoRowNum}")
    var createInfoRowNum: Int = 0

    val drop: String = "drop table "

    val create: String = "create table "

    val insert: String = "insert into "

    /**
     * 创建db
     */
    private fun createTable(sheet: Sheet, tableName: String): String {

        var sql = StringBuffer()

        sql.append(create)
        sql.append("$tableName ( ")

        // 从第五行中读取到创库信息
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

        coreDao.sqlExecute("$drop if exists $tableName;")
        coreDao.sqlExecute(sql.toString())

        fields = StringBuffer(fields.removeRange(IntRange(fields.length - 1, fields.length - 1)))
        return fields.toString()
    }

    /**
     * 读取数据并写入
     */
    private fun readAndWriteData(sheet: Sheet, tableName: String, fields: String) {

        try {

            logger.info("准备读取数据并写入$tableName, 共有${sheet.lastRowNum}行")
            var insertSql = StringBuffer(insert + tableName + " (${fields}) values ")

            // 从第x行中读取到创库信息
            for (index in (createInfoRowNum + 1)..sheet.lastRowNum) {

                val dataRow = sheet.getRow(index)?: continue

                // 如果首列ID列为空则跳过
                val firstRow = dataRow.getCell(0)
                if (firstRow == null || firstRow.cellTypeEnum == CellType.BLANK)
                    continue

                val idRowValue = ExcelUtil.getCellData(firstRow)
                if (idRowValue == "")
                    continue

                insertSql.append("(")
                var dataSB = StringBuffer("")


                for (cellIndex in 0..dataRow.lastCellNum) {

                    if (ExcelUtil.needReadData(sheet, cellIndex, createInfoRowNum)) {

                        val row = dataRow.getCell(cellIndex)
                        var data: String
                        if (row != null) {

                            data = ExcelUtil.getCellData(row)
                            if (!StringUtil.isNumber(data))
                                data = "'$data'"
                        } else // 处理空字符串
                            data = "''"
                        dataSB.append("$data,")
                    }
                }
                dataSB = StringBuffer(dataSB.removeRange(IntRange(dataSB.length - 1, dataSB.length - 1)))
                insertSql.append(dataSB.toString())
                insertSql.append("),")
            }

            insertSql = StringBuffer(insertSql.removeRange(IntRange(insertSql.length - 1, insertSql.length - 1)))
            insertSql.append(";")

            coreDao.sqlExecute(insertSql.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 处理文件,创表
     */
    fun dealFile(model: ReadModel) {

        // 目前只读取第一页
        val sheetIndex = 1
        val tName = model.name.toLowerCase()
        val it = workbook!!.getSheetAt(0)

//        workbook!!.sheetIterator().forEach {

        logger.info("读取页签${sheetIndex}:${it.sheetName}")
        logger.info("共有${it.lastRowNum + 1}行数据")

        val tableName = tablePrefix + tName
        val fields = createTable(it, tableName)
//            logger.info(fields)

        // 读取写入数据
        readAndWriteData(it, tableName, fields)
//        }
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