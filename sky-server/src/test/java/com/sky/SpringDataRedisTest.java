package com.sky;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
public class SpringDataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisTemplate(){
        System.out.println(redisTemplate);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        HashOperations hashOperations = redisTemplate.opsForHash();
        ListOperations listOperations = redisTemplate.opsForList();
        SetOperations setOperations = redisTemplate.opsForSet();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
    }

    /**
     * 操作字符串类型的数据
     */
    @Test
    public void testString(){
        //set get setex setnx
        redisTemplate.opsForValue().set("city","beijing");
        String city = (String) redisTemplate.opsForValue().get("city");
        System.out.println(city);
        redisTemplate.opsForValue().set("code","1234",3, TimeUnit.MINUTES);
        redisTemplate.opsForValue().setIfAbsent("lock","1");

    }

    /**
     * 操作哈希类型的数据
     */
    @Test
    public void testHash(){
        //hset hget hdel hkeys hvals
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put("100","name","zhangsan");
        hashOperations.put("100","age",20);
        System.out.println(hashOperations.get("100","name"));

        Set keys = hashOperations.keys("100");
        System.out.println(keys);

        List values = hashOperations.values("100");
        System.out.println(values);
        hashOperations.delete("100","age");
    }
    /**
     * 操作列表类型的数据
     */
    @Test
    public void testList(){
        //lpush lpop rpop lindex lrange
        ListOperations listOperations = redisTemplate.opsForList();
        listOperations.leftPushAll("list","a","b","c");
        listOperations.leftPush("list","d");
        System.out.println(listOperations.range("list",0,-1));
        System.out.println(listOperations.leftPop("list"));
        System.out.println(listOperations.rightPop("list"));
        System.out.println(listOperations.index("list",0));

    }
    /**
     * 操作集合类型的数据
     */
    @Test
    public void testSet(){
        //sadd srem smembers scard sinter sunion
        SetOperations setOperations = redisTemplate.opsForSet();
        setOperations.add("set","a","b","c");
        setOperations.add("set2","a","b","c","d");
        setOperations.remove("set","a");
        System.out.println(setOperations.members("set"));
        System.out.println(setOperations.size("set"));
        System.out.println(setOperations.intersect("set","set2"));
        System.out.println(setOperations.union("set","set2"));
    }
    /**
     * 操作有序集合类型的数据
     */
    @Test
    public void testZSet(){
        //zadd zrem zrange zincrby
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add("zset","a",1);
        zSetOperations.add("zset","b",2);
        System.out.println(zSetOperations.range("zset",0,-1));
        zSetOperations.incrementScore("zset","a",1);
        System.out.println(zSetOperations.score("zset","a"));
        zSetOperations.remove("zset","a");
    }


    /**
     * 通用命令操作
     *
     */
    @Test
    public void testCommon(){
        //keys exists del  type
        Set keys = redisTemplate.keys("*");
        System.out.println(keys);

        Boolean name = redisTemplate.hasKey("name");
        Boolean set1 = redisTemplate.hasKey("set1");

        for (Object key : keys) {
            DataType type = redisTemplate.type(key);
            System.out.println(type);
        }

        redisTemplate.delete("set");
    }
}

