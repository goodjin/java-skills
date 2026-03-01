# logstash

## 项目简介
# Logstash

Logstash is part of the [Elastic Stack](https://www.elastic.co/products) along with Beats, Elasticsearch and Kibana. Logstash is a server-side data processing pipeline that ingests data from a multitude of sources simultaneously, transforms it, and then sends it to your favorite "stash." (Ours is Elasticsearch, naturally.). Logstash has over 200 plugins, and you can write your own very easily as well.

For more info, see <https://www.elastic.co/products/logstash>

## Documentation and Getting Started

You can find the documentation and getting started guides for Logstash
on the [elastic.co site](https://www.elastic.co/guide/en/logstash/current/getting-started-with-logstash.html)

For information about building the documentation, see the README in https://github.com/elastic/docs

## Downloads

You can download officially released Logstash binaries, as well as debian/rpm packages for the
supported platforms, from [downloads page](https://www.elastic.co/downloads/logstash).

## Need Help?

- [Logstash Forum](https://discuss.elastic.co/c/logstash)
- [Logstash Documentation](https://www.elastic.co/guide/en/logstash/current/index.html)
- [Logstash Product Information](https://www.elastic.co/products/logstash)
- [Elastic Support](https://www.elastic.co/subscriptions)

## Logstash Plugins

Logstash plugins are hosted in separate repositories under the [logstash-plugins](https://github.com/logstash-plugins) github organization. Each plugin is a self-contained Ruby gem which gets published to RubyGems.org.

### Writing your own Plugin

Logstash is known for its extensibility. There are hundreds of plugins for Logstash and you can write your own very easily! For more info on developing and testing these plugins, please see the [working with plugins section](https://www.elastic.co/guide/en/logstash/current/contributing-to-logstash.html)

### Plugin Issues and Pull Requests

**Please open new issues and pull requests for plugins under its own repository**

For example, if you have to report an issue/enhancement for the Elasticsearch output, please do so [here](https://github.com/logstash-plugins/logstash-output-elasticsearch/issues).

Logstash core will continue to exist under this repository and all related issues and pull requests can be submitted here.

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Gradle
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
