# helios

## 项目简介
Helios [![Circle CI](https://circleci.com/gh/spotify/helios/tree/master.png?style=badge)](https://circleci.com/gh/spotify/helios/tree/master) [![Slack Status](http://slackin.spotify.com/badge.svg)](http://slackin.spotify.com) [ ![Download](https://api.bintray.com/packages/spotify/deb/helios/images/download.svg) ](https://bintray.com/spotify/deb/helios/_latestVersion)
======

## Status: Sunset

This project was created when there were no open source container orchestration frameworks.
Since the advent of Kubernetes and other tools, we've stopped using helios at Spotify
and have now switched to other tools like Kubernetes. This project will no longer accept PRs.

Helios is a Docker orchestration platform for deploying and managing
containers across an entire fleet of servers. Helios provides a HTTP
API as well as a command-line client to interact with servers running
your containers. It also keeps a history of events in your cluster including
information such as deploys, restarts and version changes.


Usage Example
-------------

```sh
# Create an nginx job using the nginx container image, exposing it on the host on port 8080
$ helios create nginx:v1 nginx:1.7.1 -p http=80:8080

# Check that the job is listed
$ helios jobs

# List helios hosts
$ helios hosts

# Deploy the nginx job on one of the hosts
$ helios deploy nginx:v1 <host>

# Check the job status
$ helios status

# Curl the nginx container when it's started running
$ curl <host>:8080

# Undeploy the nginx job
$ helios undeploy -a nginx:v1

## 整体架构描述
  architecture would need to be revisited.  Helios can also scale down
  well in that you can run a single machine instance if you want to
  run it all locally.

Other Software You Might Want To Consider
-----------------------------------------
Here are a few other things you probably want to consider using alongside
Helios:
* [docker-gc](https://github.com/spotify/docker-gc) Garbage collects dead containers and removes unused images.
* [helios-skydns](https://github.com/spotify/helios-skydns) Makes it so you can auto register services in SkyDNS.  If you use leading underscores in your SRV record names, let us know, we have a patch for etcd which disables the "hidden" node feature which makes this use case break.
* [skygc](https://github.com/spotify/skygc)  When using SkyDNS, especially if you're using the Helios Testing Framework, can leave garbage in the skydns tree within etcd.  This will clean out dead stuff.
* [docker-maven-plugin](https://github.com/spotify/docker-maven-plugin)  Simplifies the building of Docker containers if you're using Maven (and most likely Java).

Findbugs
--------

To run [findbugs](http://findbugs.sourceforge.net) on the helios codebase, do
`mvn clean compile site`. This will build helios and then run an analysis,
emitting reports in `helios-*/target/site/findbugs.html`.

To silence an irrelevant warning, add a filter match along with a justification
in `findbugs-exclude.xml`.

The Nickel Tour
---------------

The sources for the Helios master and agent are under [helios-services](helios-services).
The CLI source is under [helios-tools](helios-tools).
The Helios Java client is under [helios-client](helios-client).

The main meat of the Helios agent is in [Supervisor.java](helios-services/src/main/java/com/spotify/helios/agent/Supervisor.java),

## 核心模块划分
Other components that are required for a helios installation are:

* [Docker 1.0](https://github.com/docker/docker) or newer
* [Zookeeper 3.4.0](https://zookeeper.apache.org/) or newer


Install & Run
-------------

### Quick start for local usage
Use [helios-solo](https://github.com/spotify/helios/blob/master/docs/helios_solo.md)
to launch a local environment with a Helios master and agent.

First, ensure you have [Docker installed locally](http://docs.docker.com/engine/installation/).
Test this by making sure `docker info` works. Then install helios-solo:

```bash
# add the helios apt repository
$ sudo apt-key adv --keyserver hkp://keys.gnupg.net:80 --recv-keys 6F75C6183FF5E93D
$ echo "deb https://dl.bintray.com/spotify/deb trusty main" | sudo tee -a /etc/apt/sources.list.d/helios.list

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
