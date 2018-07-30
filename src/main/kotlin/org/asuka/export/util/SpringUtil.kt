package org.asuka.export.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * @author: orange
 * @Description:
 * @Date: Create in 2018.04.10
 */
@Component
object SpringUtil : ApplicationContextAware {

    var context: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext?) {
        this.context = applicationContext
    }

    fun <T> getBean(clazz: Class<T>): T {
        return context!!.getBean(clazz)
    }

    fun <T> getBean(name: String): T {
        return context!!.getBean(name) as T
    }
}