package org.asuka.export.util

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.DecimalFormat


/**
 * @author: orange
 * @Description:读取excel表工具类
 * @Date: Create in 2018.04.12
 */
object ExcelUtil {

    private var logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 判断是否是需要读取的字段
     */
    fun needReadData (sheet: Sheet, cellIndex: Int, rowNum: Int): Boolean {

        val theRow: Row = sheet.getRow(rowNum)
        var theCell = theRow.getCell(cellIndex)

        if (theCell == null || theCell.cellTypeEnum == CellType.BLANK || theCell.stringCellValue == "")
            return false
        return true
    }

    fun getAssignRowCellData(sheet: Sheet, cell: Cell, rowNum: Int): String {

        val theRow: Row = sheet.getRow(rowNum)
        var theCell = theRow.getCell(cell.columnIndex)

        if (theCell == null || theCell.cellTypeEnum == CellType.BLANK)
            return ""
        return theCell.stringCellValue.trim()
    }

    fun getCellData(cell: Cell): String {

        try {
            if (cell.cellTypeEnum == CellType.NUMERIC) {
                // 返回数值类型的值
                var inputValue: Any? = null// 单元格值
                val longVal = Math.round(cell.numericCellValue)
                val doubleVal = cell.numericCellValue
                if (java.lang.Double.parseDouble(longVal.toString() + ".0") == doubleVal) {   //判断是否含有小数位.0
                    inputValue = longVal
                } else {
                    inputValue = doubleVal
                }
                val df = DecimalFormat("#.####")    //格式化为四位小数，按自己需求选择；
                return df.format(inputValue).toString()      //返回String类型
            } else {
                cell.setCellType(CellType.STRING)
                return cell.stringCellValue.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("未处理的类型${cell.cellTypeEnum.name}")
        }

        throw IllegalStateException("未处理的类型${cell.cellTypeEnum.name}")
    }
}