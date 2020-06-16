package org.asuka.export.dao.dscfg

import com.zaxxer.hikari.HikariDataSource
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import javax.sql.DataSource


/**
 * @author: 援护防御
 * @mail: wangzijun@bilibili.com
 * @Description: config库的数据源映射
 * @Date: Create in 2018.04.26
 */
@Configuration
@MapperScan(basePackages = ["org.asuka.export.dao.mapper.cfg"], sqlSessionTemplateRef = "cfgSqlSessionTemplate")
class CfgDataSourceConfig {

    @Bean(name = ["cfgDataSource"])
    @Primary
    @ConfigurationProperties(prefix = "cfg.datasource") // prefix值必须是application.yml中对应属性的前缀
    fun cfgDataSource(): DataSource {
        return HikariDataSource()
    }

    @Bean
    @Throws(Exception::class)
    fun cfgSqlSessionFactory(@Qualifier("cfgDataSource") dataSource: DataSource): SqlSessionFactory {
        val sessionFactory = SqlSessionFactoryBean()
        sessionFactory.setDataSource(dataSource)
        //添加mapper目录
        val resolver = PathMatchingResourcePatternResolver()
        try {

//            var configuration: org.apache.ibatis.session.Configuration = org.apache.ibatis.session.Configuration()
//            configuration.isMapUnderscoreToCamelCase = true
//
//            sessionFactory.setConfiguration(configuration)
//
//            sessionFactory.setMapperLocations(resolver.getResource("classpath*:org/asuka/export/dao/mapper/cfg/*.xml"))
//            return sessionFactory.`object`
//            sessionFactory.setTypeAliasesPackage("com.louis.springboot.**.model") // 扫描Model


            sessionFactory.setMapperLocations(*resolver.getResources("classpath*:**/mapper/cfg/*.xml")) // 扫描映射文件
            return sessionFactory.getObject()
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

    }

    @Bean
    @Throws(Exception::class)
    fun cfgSqlSessionTemplate(@Qualifier("cfgSqlSessionFactory") sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }
}