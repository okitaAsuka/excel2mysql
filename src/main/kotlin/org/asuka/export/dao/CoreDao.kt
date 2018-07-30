package org.asuka.export.dao

import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author: 援护防御
 * @mail: wangzijun@bilibili.com
 * @Description: 简单dao调用
 * @Date: Create in 2018.05.11
 */
@Component
class CoreDao {

    @Autowired
    lateinit var cfgSqlSessionFactory: SqlSessionFactory

    fun sqlExecute(sql: String) {

        var sqlSession: SqlSession? = null
        try {

            sqlSession = cfgSqlSessionFactory.openSession(true)
            sqlSession.update("sqlExecute", sql)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            sqlSession!!.close()
        }

    }

}