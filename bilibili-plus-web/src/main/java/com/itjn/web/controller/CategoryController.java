package com.itjn.web.controller;

import com.itjn.entity.po.CategoryInfo;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.CategoryInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Validated
@RequestMapping("/category")
public class CategoryController extends ABaseController {

    @Resource
    private CategoryInfoService categoryInfoService;

    /**
     * 查询所有分类信息
     * @return
     */
    @RequestMapping("/loadAllCategory")
    public ResponseVO loadAllCategory() {
        List<CategoryInfo> categoryInfoList = categoryInfoService.getAllCategoryList();
        return getSuccessResponseVO(categoryInfoList);
    }

}
