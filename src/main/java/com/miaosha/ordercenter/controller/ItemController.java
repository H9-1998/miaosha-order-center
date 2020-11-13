package com.miaosha.ordercenter.controller;

import com.miaosha.ordercenter.model.ItemModel;
import com.miaosha.ordercenter.response.CommonReturnType;
import com.miaosha.ordercenter.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public CommonReturnType getItemById(@RequestParam("itemId") Integer itemId){
        ItemModel itemModel = itemService.getItemByItemId(itemId);
        return CommonReturnType.create(itemModel);
    }
}
