package org.asuka.export.dao.dscfg

import com.alibaba.druid.pool.DruidDataSource
import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Configuration
import org.mybatis.spring.SqlSessionTemplate
import org.apache.ibatis.session.SqlSessionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Bean
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.sql.DataSource


/**
 * @author: 援护防御
 * @mail: wangzijun@bilibili.com
 * @Description: config库的数据源映射
 * @Date: Create in 2018.04.26
 */
@Configuration
@MapperScan(basePackages = arrayOf("com.biligame.darkboom.dao.mapper.cfg"), sqlSessionTemplateRef = "cfgSqlSessionTemplate")
class CfgDataSourceConfig {

    @Bean(name = arrayOf("cfgDataSource"))
    @Primary
    @ConfigurationProperties(prefix = "cfg.datasource") // prefix值必须是application.yml中对应属性的前缀
    fun cfgDataSource(): DataSource {
        return DruidDataSource()
    }

    @Bean
    @Throws(Exception::class)
    fun cfgSqlSessionFactory(@Qualifier("cfgDataSource") dataSource: DataSource): SqlSessionFactory {
        val bean = SqlSessionFactoryBean()
        bean.setDataSource(dataSource)
        //添加mapper目录
        val resolver = PathMatchingResourcePatternResolver()
        try {

            var configuration: org.apache.ibatis.session.Configuration = org.apache.ibatis.session.Configuration()
            configuration.isMapUnderscoreToCamelCase = true

            bean.setConfiguration(configuration)
            bean.setMapperLocations(resolver.getResources("classpath*:com/biligame/darkboom/dao/mapper/cfg/*.xml"))
            return bean.`object`
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