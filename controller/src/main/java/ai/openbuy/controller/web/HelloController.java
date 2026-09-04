package ai.openbuy.controller.web;

import ai.openbuy.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

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
