
package org.asuka.export

import org.asuka.export.read.ExcelReader
import org.asuka.export.util.SpringUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import kotlin.system.exitProcess

/**
 * @author: 援护防御
 * @mail: wangzijun@bilibili.com
 * @Description: TODO
 * @Date: Create in 2018.05.09
 */
@SpringBootApplication
class ExportData {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    var readLst: ArrayList<String> = arrayListOf()

    @Value("\${readFilesName}")
    lateinit var readFilesName: String

    private fun init() {

        if (readFilesName == "") {
            logger.error("没有配置需要读取的文件!")
            exitProcess(0)
        }

        for (s in readFilesName.split(",")) {
            readLst.add(s)
        }
    }

    fun exportData() {

        init()
        val reader: ExcelReader = SpringUtil.getBean(ExcelReader::class.java)

        readLst.forEach {

            // 读取文件
            reader.readFile(it)
            // 处理文件
            reader.dealFile(it)
        }
    }
}

fun main(args: Array<String>) {

    val ctx = SpringApplication.run(ExportData::class.java, *args)
    ctx.getBean(ExportData::class.java).exportData()
}