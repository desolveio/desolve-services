package io.desolve.services.core

import org.reflections.Reflections
import org.reflections.Store
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.Scanners
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.QueryFunction
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 5/25/2022
 */
class DesolveServiceLocator(
    private val clazz: KClass<*>,
    private val packageName: String
)
{
    val reflections =
        Reflections(
            ConfigurationBuilder()
                .forPackage(
                    this.packageName,
                    this.clazz.java.classLoader
                )
                .addScanners(
                    MethodAnnotationsScanner(),
                    TypeAnnotationsScanner(),
                    SubTypesScanner()
                )
        )

    inline fun <reified T> getSubTypes(): List<Class<*>>
    {
        return reflections
            .get(subTypes<T>())
            .toList()
    }

    inline fun <reified T : Annotation> getMethodsAnnotatedWith(): List<Method>
    {
        return reflections
            .get(annotated<T>())
            .toList()
    }

    inline fun <reified T : Annotation> getTypesAnnotatedWith(): List<Class<*>>
    {
        return reflections
            .getTypesAnnotatedWith(T::class.java)
            .toList()
    }

    inline fun <reified T> annotated(): QueryFunction<Store, Method>
    {
        return Scanners.MethodsAnnotated
            .with(T::class.java)
            .`as`(Method::class.java)
    }

    fun <T> getSubTypes(
        kClass: KClass<T & Any>
    ): List<Class<*>>
    {
        return reflections
            .get(
                Scanners.SubTypes
                    .with(kClass.java)
                    .`as`(Class::class.java)
            )
            .toList()
    }

    inline fun <reified T> subTypes(): QueryFunction<Store, Class<*>>
    {
        return Scanners.SubTypes
            .with(T::class.java)
            .`as`(Class::class.java)
    }
}
