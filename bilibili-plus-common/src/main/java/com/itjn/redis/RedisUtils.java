package com.itjn.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component("redisUtils")
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean keyExists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(毫秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean setex(String key, V value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.MILLISECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<V> getQueueList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }


    public boolean lpush(String key, V value, Long time) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            if (time != null && time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long remove(String key, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, 1, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean lpushAll(String key, List<V> values, long time) {
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public V rpop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key, 1);
        return count;
    }

    /**
     * 递增并设置过期时间
     * @param key
     * @param milliseconds
     * @return
     */
    public Long incrementex(String key, long milliseconds) {
        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count == 1) {
            expire(key, milliseconds);
        }
        return count;
    }

    public Long decrement(String key) {
        Long count = redisTemplate.opsForValue().increment(key, -1);
        if (count <= 0) {
            redisTemplate.delete(key);
        }
        logger.info("key:{},减少数量{}", key, count);
        return count;
    }


    public Set<String> getByKeyPrefix(String keyPrifix) {
        Set<String> keyList = redisTemplate.keys(keyPrifix + "*");
        return keyList;
    }

    /**
     * 批量获取视频播放量
     * @param keyPrifix
     * @return
     */
    public Map<String, V> getBatch(String keyPrifix) {
        //通过 key前缀 + 通配符 匹配获取所有的key
        Set<String> keySet = redisTemplate.keys(keyPrifix + "*");
        List<String> keyList = new ArrayList<>(keySet);
        //批量获取key对应的value
        List<V> valueList = redisTemplate.opsForValue().multiGet(keyList);
        //将key和value对应起来，封装为一个Map<String, Integer>
        Map<String, V> resultMap = keyList.stream().collect(
                Collectors.toMap(key -> key, value -> valueList.get(keyList.indexOf(value))));
        return resultMap;
    }

    /**
     * 添加有序集合
     * @param key
     * @param v
     */
    public void zaddCount(String key, V v) {
        redisTemplate.opsForZSet().incrementScore(key, v, 1);
    }


    public List<V> getZSetList(String key, Integer count) {
        Set<V> topElements = redisTemplate.opsForZSet().reverseRange(key, 0, count);
        List<V> list = new ArrayList<>(topElements);
        return list;
    }

}
