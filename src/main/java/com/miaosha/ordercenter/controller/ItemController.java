package com.miaosha.ordercenter.controller;

import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.model.ItemModel;
import com.miaosha.ordercenter.response.CommonReturnType;
import com.miaosha.ordercenter.service.ItemService;
import com.miaosha.ordercenter.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @auhor: dhz
 * @date: 2020/11/13 18:10
 */
@RestController
@Api(tags = {"商品相关api"})
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取所有商品
     * @return
     */
    @GetMapping("/list-item")
    @ApiOperation("获取所有商品")
    public CommonReturnType listItem(){
        List<ItemModel> itemModels = itemService.listItem();
        return CommonReturnType.create(itemModels);
    }

    @GetMapping("/get-item-by-id")
    @ApiOperation("根据id获取商品")
    public CommonReturnType getItemById(@RequestParam("itemId") Integer itemId) throws BusinessException {
        ItemModel itemModel = itemService.getItemByItemIdInRedis(itemId);
        return CommonReturnType.create(itemModel);
    }

    /**
     * 将商品添加进购物车
     * @param token     登录凭证
     * @param itemId    加购商品id
     * @param amount    加购数量
     * @return
     */
    @PostMapping("/add-into-shoppingCart")
    @ApiOperation("将商品添加进购物车")
    public CommonReturnType addItemIntoShoppingCart(@RequestHeader("x-token") String token,
                                                    @RequestParam("itemId") Integer itemId,
                                                    @RequestParam("amount") Integer amount){
        // 从token中取出用户id
        Integer userId = jwtUtil.getUserIdFromToken(token);
        Boolean res = itemService.addItemIntoShoppingCart(itemId, amount, userId);
        if (res == true)
            return CommonReturnType.create("添加购物车成功");
        else
            return CommonReturnType.create("添加购物车失败");
    }

    /**
     * 查询某个用户的购物车
     * @param token     登录凭证
     * @return
     */
    @GetMapping("/get-shoppingCart")
    @ApiOperation("查询某个用户的购物车")
    public CommonReturnType getShoppingCartByUserId(@RequestHeader("x-token") String token){
        Integer userId = jwtUtil.getUserIdFromToken(token);
        return itemService.getShoppingCartByUserId(userId);
    }

    /**
     * 清空购物车
     * @param token
     * @return
     */
    @PutMapping("/clean-shoppingCart")
    @ApiOperation("清空购物车")
    public CommonReturnType cleanShoppingCart(@RequestHeader("x-token") String token){
        Integer userId = jwtUtil.getUserIdFromToken(token);
        return itemService.cleanShoppingCart(userId);
    }
}
