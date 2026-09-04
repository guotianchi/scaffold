package ai.openbuy.controller.web;

import ai.openbuy.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供 Hello 相关 HTTP 接口的控制器。
 */
@RestController
public class HelloController {

    private final HelloService helloService;

    /**
     * 通过依赖注入创建控制器实例。
     *
     * @param helloService 处理 Hello 业务逻辑的服务
     */
    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    /**
     * 返回 Hello 问候语。
     *
     * @return 由 {@link HelloService} 提供的问候字符串
     */
    @GetMapping("/hello")
    public String hello() {
        return helloService.hello();
    }

    /**
     * 用于验证 PR 流程的占位方法。
     *
     * @return 固定测试字符串 {@code "testPr"}
     */
    public String testPr() {
        return "testPr";
    }
}
