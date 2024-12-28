package com.itjn.web;

import com.itjn.component.EsSearchComponent;
import com.itjn.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component("initRun")
public class InitRun implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Override
    public void run(ApplicationArguments args) {

        Connection connection = null;
        Boolean startSuccess = true;
        try {
            connection = dataSource.getConnection();
            redisUtils.get("test");

            esSearchComponent.createIndex();

            logger.error("服务启动成功，可以开始愉快的开发了");
        } catch (SQLException e) {
            logger.error("数据库配置错误，请检查数据库配置");
            startSuccess = false;
        } catch (Exception e) {
            logger.error("服务启动失败", e);
            startSuccess = false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!startSuccess) {
                System.exit(0);
            }
        }

    }

}
