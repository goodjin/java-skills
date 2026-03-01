# helios

## 核心类和接口

AgentInfo
AuthenticatingHttpConnector
Clock
CreateDeploymentGroupResponse
CreateJobResponse
DefaultHttpConnector
Deployment
DeploymentGroup
DeploymentGroupStatus
DeploymentGroupStatusResponse
DeploymentGroupTasks
DockerVersion
Endpoint
Endpoints
ExecHealthCheck

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: HostStatus,PortMapping,DockerVersion
- 工厂模式: Resolver,Json,Endpoints
- 策略模式: 未明显发现
- 装饰器模式: HeliosClient,RetryingRequestDispatcher
- 单例模式: Endpoints

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
