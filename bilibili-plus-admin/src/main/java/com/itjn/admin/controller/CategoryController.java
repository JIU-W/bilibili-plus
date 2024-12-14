package com.itjn.admin.controller;

import com.itjn.entity.po.CategoryInfo;
import com.itjn.entity.query.CategoryInfoQuery;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.CategoryInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/category")
@Validated
public class CategoryController extends ABaseController{

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    @Resource
    private CategoryInfoService categoryInfoService;

    /**
     * 根据条件进行分页查询分类列表
     * @param categoryInfo
     * @return
     */
    @RequestMapping("/loadCategory")
    public ResponseVO loadAllCategory(CategoryInfoQuery categoryInfo) {
        //设置根据数据库的sort字段排序
        categoryInfo.setOrderBy("sort asc");
        //将查询结果转换为树形结构
        categoryInfo.setConvert2Tree(true);
        //分页查询
        List<CategoryInfo> categoryInfoList = categoryInfoService.findListByParam(categoryInfo);
        return getSuccessResponseVO(categoryInfoList);
    }

    /**
     * 新增或修改分类
     * @param pCategoryId 分类父级id
     */
    @RequestMapping("/saveCategory")
    public ResponseVO saveCategory(@NotNull Integer pCategoryId,
                                   Integer categoryId,//修改的时候前端才会传categoryId过来
                                   @NotEmpty String categoryCode, @NotEmpty String categoryName,
                                   String icon, String background) {
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setPCategoryId(pCategoryId);
        categoryInfo.setCategoryId(categoryId);
        categoryInfo.setCategoryCode(categoryCode);
        categoryInfo.setCategoryName(categoryName);
        categoryInfo.setIcon(icon);
        categoryInfo.setBackground(background);
        //新增分类或修改分类
        categoryInfoService.saveCategoryInfo(categoryInfo);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除分类
     * @param categoryId
     * @return
     */
    @RequestMapping("/delCategory")
    public ResponseVO delCategory(@NotNull Integer categoryId) {
        categoryInfoService.delCategory(categoryId);
        return getSuccessResponseVO(null);
    }




}
