package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Profile("dev & mock-pay")
@RestController
@RequestMapping("/user/order")
@RequiredArgsConstructor
public class MockPaymentController {

    private final OrderService orderService;

    @PutMapping("/mock-payment")
    public Result<Void> mockPayment(@RequestBody OrdersPaymentDTO request) {
        orderService.mockPayment(request.getOrderNumber());
        return Result.success();
    }
}
