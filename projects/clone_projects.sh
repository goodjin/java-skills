#!/bin/bash

# 已存在的项目(不区分大小写)
EXISTING_PROJECTS=(
  "rxjava" "sentinel" "activiti" "arthas" "assertj" "dubbo" "feign" "guava" 
  "hutool" "jaeger" "jeecg-boot" "junit5" "mockito" "my-rpc" "my-spring-boot" 
  "mybatis-plus" "nacos" "netty" "okhttp" "pulsar" "reactor" "redisson" 
  "retrofit" "rocketmq" "shiro" "skywalking" "spring-boot" "spring-cloud" "zipkin"
)

# 需要克隆的高质量Java项目列表
PROJECTS=(
  # 构建工具
  "apache/maven"
  "gradle/gradle"
  
  # ORM/数据库
  "hibernate/hibernate-orm"
  "mybatis/mybatis-3"
  "jOOQ/jOOQ"
  "spring-projects/spring-data-jpa"
  "flyway/flyway"
  "liquibase/liquibase"
  "brettwooldridge/HikariCP"
  "alibaba/druid"
  "apache/shardingsphere"
  "debezium/debezium"
  "jdbc-essentials/h2database"
  
  # JSON
  "FasterXML/jackson"
  "google/gson"
  
  # 工具类
  "projectlombok/lombok"
  "apache/commons-lang"
  "apache/commons-collections"
  "apache/commons-io"
  
  # 日志
  "qos-ch/logback"
  "apache/logging-log4j2"
  "qos-ch/slf4j"
  
  # 测试
  "testng/testng"
  "spockframework/spock"
  "powermock/powermock"
  "openjdk/jmh"
  
  # HTTP
  "apache/httpcomponents-client"
  
  # 分布式/微服务
  "Netflix/eureka"
  "Netflix/hystrix"
  "Netflix/zuul"
  "alibaba/seata"
  "ctripcorp/apollo"
  "alibaba/fescar"
  
  # 消息队列
  "apache/kafka"
  "rabbitmq/rabbitmq-server"
  
  # 搜索
  "elastic/elasticsearch"
  "apache/solr"
  
  # 监控
  "prometheus/client_java"
  "naver/pinpoint"
  "dianping/cat"
  
  # 缓存
  "ben-manes/caffeine"
  "ehcache/ehcache"
  "xetorthio/jedis"
  "lettuce-io/lettuce-core"
  
  # 高性能
  "LMAX-Exchange/disruptor"
  "akka/akka"
  
  # 工作流
  "flowable/flowable-engine"
  
  # 字节码
  "llpdf/asm"
  "cglib/cglib"
  "jboss-javassist/javassist"
  "bytebuddy/bytebuddy"
  
  # 依赖注入
  "google/guice"
  "square/dagger"
  
  # 配置
  "hashicorp/consul"
  
  # API文档
  "swagger-api/swagger-core"
  "swagger-api/swagger-ui"
  
  # 序列化
  "protocolbuffers/protobuf"
  "google/flatbuffers"
  
  # RPC
  "grpc/grpc-java"
  "apache/thrift"
  
  # 其他
  "google/error-prone"
  "alibaba/p3c"
  "alibaba/transmittable-thread-local"
  "alibaba/ARouter"
)

# 检查项目是否已存在
is_exist() {
  local repo=$1
  local name=$(echo $repo | cut -d'/' -f2 | tr '[:upper:]' '[:lower:]')
  for existing in "${EXISTING_PROJECTS[@]}"; do
    if [[ "$name" == "$(echo $existing | tr '[:upper:]' '[:lower:]')" ]]; then
      return 0
    fi
  done
  if [[ -d "$name" ]]; then
    return 0
  fi
  return 1
}

# 克隆项目
clone_project() {
  local repo=$1
  local name=$(echo $repo | cut -d'/' -f2)
  
  if is_exist "$repo"; then
    echo "SKIP (exists): $repo"
    return 1
  fi
  
  echo "CLONING: $repo"
  git clone --depth 1 "https://github.com/$repo.git" "$name" 2>&1 | tail -3
  return 0
}

COUNT=0
for proj in "${PROJECTS[@]}"; do
  if clone_project "$proj"; then
    ((COUNT++))
  fi
done

echo "========================="
echo "新增项目数: $COUNT"
