package com.zjut.spring.boot.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisDefaultParameterHandler;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.core.parser.SqlInfo;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlParserUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 改编自mybatis plus 自带的PaginationInterceptor插件
 * 修改点：
 * 在原先的基础上增加了对查询分页，如果没有加limit则加上limit，如果加上了，则判断并限制单次查询的个数
 */
@Intercepts({@Signature(
        type = StatementHandler.class,
        method = "prepare",
        args = {Connection.class, Integer.class}
)})
public class FactoryPaginationInterceptor extends AbstractSqlParserHandler implements Interceptor {

    private final static Logger logger = LoggerFactory.getLogger(FactoryPaginationInterceptor.class);

    private ISqlParser sqlParser;
    private boolean overflow = false;
    private String dialectType;
    private String dialectClazz;

    public FactoryPaginationInterceptor() {
    }

    public static String concatOrderBy(String originalSql, IPage page, boolean orderBy) {
        if (!orderBy || !ArrayUtils.isNotEmpty(page.ascs()) && !ArrayUtils.isNotEmpty(page.descs())) {
            return originalSql;
        } else {
            StringBuilder buildSql = new StringBuilder(originalSql);
            String ascStr = concatOrderBuilder(page.ascs(), " ASC");
            String descStr = concatOrderBuilder(page.descs(), " DESC");
            if (StringUtils.isNotEmpty(ascStr) && StringUtils.isNotEmpty(descStr)) {
                ascStr = ascStr + ", ";
            }

            if (StringUtils.isNotEmpty(ascStr) || StringUtils.isNotEmpty(descStr)) {
                buildSql.append(" ORDER BY ").append(ascStr).append(descStr);
            }

            return buildSql.toString();
        }
    }

    private static String concatOrderBuilder(String[] columns, String orderWord) {
        return ArrayUtils.isNotEmpty(columns) ? (String) Arrays.stream(columns).filter(StringUtils::isNotEmpty).map((i) -> {
            return i + orderWord;
        }).collect(Collectors.joining(",")) : "";
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        this.sqlParser(metaObject);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        } else {
            BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
            Object paramObj = boundSql.getParameterObject();
            IPage page = null;
            if (paramObj instanceof IPage) {
                page = (IPage) paramObj;
            } else if (paramObj instanceof Map) {
                Iterator var8 = ((Map) paramObj).values().iterator();

                while (var8.hasNext()) {
                    Object arg = var8.next();
                    if (arg instanceof IPage) {
                        page = (IPage) arg;
                        break;
                    }
                }
            }

            String originalSql = boundSql.getSql();
            Connection connection = (Connection) invocation.getArgs()[0];
            DbType dbType = StringUtils.isNotEmpty(this.dialectType) ? DbType.getDbType(this.dialectType) : JdbcUtils.getDbType(connection.getMetaData().getURL());

            // 判断是否使用Mybatis plus自带的Page分页，如果不是需要对SQL判断
            if (null != page && page.getSize() >= 0L) {
                boolean orderBy = true;
                if (page.isSearchCount()) {
                    SqlInfo sqlInfo = SqlParserUtils.getOptimizeCountSql(page.optimizeCountSql(), this.sqlParser, originalSql);
                    orderBy = sqlInfo.isOrderBy();
                    this.queryTotal(this.overflow, sqlInfo.getSql(), mappedStatement, boundSql, page, connection);
                    if (page.getTotal() <= 0L) {
                        return invocation.proceed();
                    }
                }

                String buildSql = concatOrderBy(originalSql, page, orderBy);
                // 如果传进来的单次查询数量大于阈值，则修改传进来的最大阈值
                if (page.getSize() > 500) {
                    page.setSize(500);
                }
                DialectModel model = DialectFactory.buildPaginationSql(page, buildSql, dbType, this.dialectClazz);
                Configuration configuration = mappedStatement.getConfiguration();
                List<ParameterMapping> mappings = new ArrayList(boundSql.getParameterMappings());
                Map<String, Object> additionalParameters = (Map) metaObject.getValue("delegate.boundSql.additionalParameters");
                model.consumers(mappings, configuration, additionalParameters);
                metaObject.setValue("delegate.boundSql.sql", model.getDialectSql());
                metaObject.setValue("delegate.boundSql.parameterMappings", mappings);
                metaObject.setValue("delegate.rowBounds.offset", 0);
                metaObject.setValue("delegate.rowBounds.limit", 2147483647);
                return invocation.proceed();
            } else {
                // 如果是查数量的就不要分页了
                if (org.apache.commons.lang3.StringUtils.isNotBlank(originalSql) && (originalSql.contains("count(") || originalSql.contains("COUNT("))) {
                    return invocation.proceed();
                }

                metaObject.setValue("delegate.boundSql.sql", this.paraseLimitSQl(originalSql));
//                metaObject.setValue("delegate.boundSql.parameterMappings", mappings);
                metaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
                metaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
                return invocation.proceed();
            }
        }
    }

    /**
     * 解析SQL中的limit，并判断单次查询是否达到设定的阈值
     * @param originalSql
     * @return
     */
    private String paraseLimitSQl(String originalSql) {
        // 如果最后面有";"，去掉
        originalSql = originalSql.replace(";", "");

        // 判断SQL中是否有limit，有的话是limit 10  还是limit 0,10
        String sqlToLowerCase = originalSql.toLowerCase();
        if (sqlToLowerCase.contains("limit")) {
            String sqlWithNolimit = originalSql.substring(0, sqlToLowerCase.lastIndexOf("limit"));
            String afterLimit = originalSql.substring(sqlToLowerCase.lastIndexOf("limit") + 5, sqlToLowerCase.length());
            String[] offsetAndSize = afterLimit.split(",");
            if (offsetAndSize.length == 1 && Integer.parseInt(offsetAndSize[0])>=500) {
                originalSql = concatSql(sqlWithNolimit, "0", "500");
            } else if(offsetAndSize.length == 2 && Integer.parseInt(offsetAndSize[1])>=500) {
                originalSql = concatSql(sqlWithNolimit, offsetAndSize[0], "500");
            }
        } else {
            originalSql += " limit 0,500";
        }
        return originalSql;
    }

    /**
     * 把limit后面的组装起来
     * @param sqlWithNoLimit
     * @param offset
     * @param size
     * @return
     */
    private String concatSql(String sqlWithNoLimit, String offset, String size) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(offset)) {
            return sqlWithNoLimit + "limit " + offset + "," + size;
        } else {
            return sqlWithNoLimit + "limit " + size;
        }

    }

    protected void queryTotal(boolean overflowCurrent, String sql, MappedStatement mappedStatement, BoundSql boundSql, IPage page, Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            Throwable var8 = null;

            try {
                DefaultParameterHandler parameterHandler = new MybatisDefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql);
                parameterHandler.setParameters(statement);
                long total = 0L;
                ResultSet resultSet = statement.executeQuery();
                Throwable var13 = null;


                try {
                    if (resultSet.next()) {
                        total = resultSet.getLong(1);
                    }
                } catch (Throwable var38) {
                    var13 = var38;
                    throw var38;
                } finally {
                    if (resultSet != null) {
                        if (var13 != null) {
                            try {
                                resultSet.close();
                            } catch (Throwable var37) {
                                var13.addSuppressed(var37);
                            }
                        } else {
                            resultSet.close();
                        }
                    }

                }

                page.setTotal(total);
                long pages = page.getPages();
                if (overflowCurrent && page.getCurrent() > pages) {
                    page.setCurrent(1L);
                }
            } catch (Throwable var40) {
                var8 = var40;
                throw var40;
            } finally {
                if (statement != null) {
                    if (var8 != null) {
                        try {
                            statement.close();
                        } catch (Throwable var36) {
                            var8.addSuppressed(var36);
                        }
                    } else {
                        statement.close();
                    }
                }

            }

        } catch (Exception var42) {
            throw ExceptionUtils.mpe("Error: Method queryTotal execution error.", var42, new Object[0]);
        }
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties prop) {
        String dialectType = prop.getProperty("dialectType");
        String dialectClazz = prop.getProperty("dialectClazz");
        if (StringUtils.isNotEmpty(dialectType)) {
            this.dialectType = dialectType;
        }

        if (StringUtils.isNotEmpty(dialectClazz)) {
            this.dialectClazz = dialectClazz;
        }

    }

    public FactoryPaginationInterceptor setSqlParser(ISqlParser sqlParser) {
        this.sqlParser = sqlParser;
        return this;
    }

    public FactoryPaginationInterceptor setOverflow(boolean overflow) {
        this.overflow = overflow;
        return this;
    }

    public FactoryPaginationInterceptor setDialectType(String dialectType) {
        this.dialectType = dialectType;
        return this;
    }

    public FactoryPaginationInterceptor setDialectClazz(String dialectClazz) {
        this.dialectClazz = dialectClazz;
        return this;
    }
}
