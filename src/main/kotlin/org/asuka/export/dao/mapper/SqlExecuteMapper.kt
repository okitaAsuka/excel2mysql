package org.asuka.export.dao.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Update

/**
 * @author: 援护防御
 * @mail: wangzijun@bilibili.com
 * @Description: TODO
 * @Date: Create in 2018.05.11
 */
@Mapper
interface SqlExecuteMapper {

    @Update("\${sql}")
    fun sqlExecute(sql: String)
}