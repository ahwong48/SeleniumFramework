package FrameworkBase;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;


public class RedisConnection {
    private JedisCluster cluster;
    private static PropertyLoader config = new PropertyLoader(separatorCompatibility("./properties/config.properties"));
    private String host;

    //https://github.com/redis/jedis#connecting-to-a-redis-cluster
    public RedisConnection() {
        if(config.getProperty("env").equals("prod")) {
            host = config.getProperty("prodRedisHost");
        }
        else if(config.getProperty("env").equals("staging")) {
            host = config.getProperty("stagingRedisHost") ;
        }
        else if(config.getProperty("env").equals("dev")) {
            host = config.getProperty("devRedisHost");
        }

        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        jedisClusterNodes.add(new HostAndPort(host, 6379));
        cluster = new JedisCluster(jedisClusterNodes);// , config)
    }

    public JedisCluster getCluster() {
        return cluster;
    }

    public static String separatorCompatibility(String filepath) {
        return filepath.replace("/", System.getProperty("file.separator"));
    }
}