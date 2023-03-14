package com.example.tobi.integrated.db

import com.example.tobi.integrated.*
import org.apache.commons.dbcp2.BasicDataSource
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource


private const val DB_NAME = "tobi"
private const val DATA_SOURCE_PROPERTIES = "db.$DB_NAME"
private const val DBCP_PROPERTIES = "$DATA_SOURCE_PROPERTIES.datasource"
private const val PROPERTY_SOURCE = "classpath:jdbc-$DB_NAME.properties"
private const val DATA_SOURCE_BEAN_NAME = "${DB_NAME}DataSource"


private const val BASE_PACKAGE_MYBATIS = "com.example.tobi.integrated.db.mapper"
private const val BASE_PACKAGE_REPOSITORY_JPA = "com.example.tobi.integrated.db.repository"
private const val BASE_PACKAGE_ENTITY_JPA = "com.example.tobi.integrated.db.entity"


private const val SQL_SESSION_TEMPLATE_REF = "${DB_NAME}SqlSessionTemplate"
private const val SQL_SESSION_FACTORY_BEAN_NAME = "${DB_NAME}SqlSessionFactoryBean"


@Configuration
@PropertySources(
    PropertySource(PROPERTY_SOURCE)
)
@EnableJpaRepositories("$BASE_PACKAGE_REPOSITORY_JPA")
@EntityScan(basePackages = [BASE_PACKAGE_ENTITY_JPA])
@MapperScan(
    basePackages = [BASE_PACKAGE_MYBATIS],
    sqlSessionTemplateRef = SQL_SESSION_TEMPLATE_REF
)
class DbConfig {
    @Bean
    @ConfigurationProperties(DATA_SOURCE_PROPERTIES)
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    //dbcp 설정값은 강제화한다
    //BasicData Source의 경우 start 메소드 호출해줘야 커넥션 풀 초기화함 안해도 되지만 첫요청이 느림
    @Bean(DATA_SOURCE_BEAN_NAME, initMethod = "start")
    @Primary
    @ConfigurationProperties(DBCP_PROPERTIES)
    fun dataSource(
        @Value("\${$DBCP_PROPERTIES.initialSize}") initialSize: Int,
        @Value("\${$DBCP_PROPERTIES.minIdle}") minIdle: Int,
        @Value("\${$DBCP_PROPERTIES.maxIdle}") maxIdle: Int,
        @Value("\${$DBCP_PROPERTIES.maxTotal}") maxTotal: Int,
        @Value("\${$DBCP_PROPERTIES.maxWaitMillis}") maxWaitMillis: Long,
        @Value("\${$DBCP_PROPERTIES.isPoolPreparedStatements}") isPoolPreparedStatements: Boolean,
        @Value("\${$DBCP_PROPERTIES.maxOpenPreparedStatements}") maxOpenPreparedStatements: Int,
        @Value("\${$DBCP_PROPERTIES.testOnBorrow}") testOnBorrow: Boolean,
        @Value("\${$DBCP_PROPERTIES.testOnReturn}") testOnReturn: Boolean,
        @Value("\${$DBCP_PROPERTIES.testWhileIdle}") testWhileIdle: Boolean,
        @Value("\${$DBCP_PROPERTIES.timeBetweenEvictionRunsMillis}") timeBetweenEvictionRunsMillis: Long,
        @Value("\${$DBCP_PROPERTIES.numTestsPerEvictionRun}") numTestsPerEvictionRun: Int,
        @Value("\${$DBCP_PROPERTIES.minEvictableIdleTimeMillis}") minEvictableIdleTimeMillis: Long,
    ): BasicDataSource {
        return dataSourceProperties().initializeDataSourceBuilder().type(BasicDataSource::class.java).build().apply {
            this.initialSize = initialSize
            this.minIdle = minIdle
            this.maxIdle = maxIdle
            this.maxTotal = maxTotal //maxActive
            this.maxWaitMillis = maxWaitMillis //maxWait
            this.isPoolPreparedStatements = isPoolPreparedStatements
            this.maxOpenPreparedStatements = maxOpenPreparedStatements
            this.testOnBorrow = testOnBorrow
            this.testOnReturn = testOnReturn
            this.testWhileIdle = testWhileIdle
            this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis
            this.numTestsPerEvictionRun = numTestsPerEvictionRun
            this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis
        }
    }

    @Bean(SQL_SESSION_FACTORY_BEAN_NAME)
    fun sqlSessionFactoryBean(@Qualifier(DATA_SOURCE_BEAN_NAME) dataSource: DataSource): SqlSessionFactoryBean {
        return SqlSessionFactoryBean().apply {
            setDataSource(dataSource)
            setConfiguration(org.apache.ibatis.session.Configuration().apply {
                //jdbcTypeForNull = JdbcType.NULL//파라미터 널일경우 처리 안함 select쿼리 안에서 null체크 해서 처리강제
                isMapUnderscoreToCamelCase = false
                isCallSettersOnNulls = true
            })
//            setTypeAliasesPackage(BASE_HANDLER_PACKAGE)
        }
    }

    @Bean(SQL_SESSION_TEMPLATE_REF)
    fun sqlSessionTemplate(@Qualifier(SQL_SESSION_FACTORY_BEAN_NAME) factory: SqlSessionFactory): SqlSessionTemplate {
        return SqlSessionTemplate(factory)
    }
}