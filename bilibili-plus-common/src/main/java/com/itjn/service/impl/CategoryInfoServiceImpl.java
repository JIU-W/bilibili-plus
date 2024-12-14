package com.itjn.service.impl;

import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.po.CategoryInfo;
import com.itjn.entity.query.CategoryInfoQuery;
import com.itjn.entity.query.SimplePage;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.exception.BusinessException;
import com.itjn.mappers.CategoryInfoMapper;
import com.itjn.service.CategoryInfoService;
import com.itjn.service.VideoInfoService;
import com.itjn.utils.StringTools;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 分类信息 业务接口实现
 */
@Service("categoryInfoService")
public class CategoryInfoServiceImpl implements CategoryInfoService {


    @Resource
    private CategoryInfoMapper<CategoryInfo, CategoryInfoQuery> categoryInfoMapper;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表(将查询结果转换为树形结构)
     *
     */
    @Override
    public List<CategoryInfo> findListByParam(CategoryInfoQuery param) {
        List<CategoryInfo> categoryInfoList = this.categoryInfoMapper.selectList(param);
        if (param.getConvert2Tree() != null && param.getConvert2Tree()) {
            //将查询结果转换为树形结构
            categoryInfoList = convertLine2Tree(categoryInfoList, Constants.ZERO);
        }
        return categoryInfoList;
    }

    //使用递归的方法：将查询结果转换为树形结构
    private List<CategoryInfo> convertLine2Tree(List<CategoryInfo> dataList, Integer pid) {
        List<CategoryInfo> children = new ArrayList();
        for (CategoryInfo m : dataList) {
            if (m.getCategoryId() != null && m.getpCategoryId() != null
                    && m.getpCategoryId().equals(pid)) {
                m.setChildren(convertLine2Tree(dataList, m.getCategoryId()));
                children.add(m);
            }
        }
        return children;
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(CategoryInfoQuery param) {
        return this.categoryInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<CategoryInfo> list = this.findListByParam(param);
        PaginationResultVO<CategoryInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(CategoryInfo bean) {
        return this.categoryInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<CategoryInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.categoryInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<CategoryInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.categoryInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(CategoryInfo bean, CategoryInfoQuery param) {
        StringTools.checkParam(param);
        return this.categoryInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(CategoryInfoQuery param) {
        StringTools.checkParam(param);
        return this.categoryInfoMapper.deleteByParam(param);
    }

    /**
     * 根据CategoryId获取对象
     */
    @Override
    public CategoryInfo getCategoryInfoByCategoryId(Integer categoryId) {
        return this.categoryInfoMapper.selectByCategoryId(categoryId);
    }

    /**
     * 根据CategoryId修改
     */
    @Override
    public Integer updateCategoryInfoByCategoryId(CategoryInfo bean, Integer categoryId) {
        return this.categoryInfoMapper.updateByCategoryId(bean, categoryId);
    }

    /**
     * 根据CategoryId删除
     */
    @Override
    public Integer deleteCategoryInfoByCategoryId(Integer categoryId) {
        return this.categoryInfoMapper.deleteByCategoryId(categoryId);
    }

    /**
     * 根据CategoryCode获取对象
     */
    @Override
    public CategoryInfo getCategoryInfoByCategoryCode(String categoryCode) {
        return this.categoryInfoMapper.selectByCategoryCode(categoryCode);
    }

    /**
     * 根据CategoryCode修改
     */
    @Override
    public Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode) {
        return this.categoryInfoMapper.updateByCategoryCode(bean, categoryCode);
    }

    /**
     * 根据CategoryCode删除
     */
    @Override
    public Integer deleteCategoryInfoByCategoryCode(String categoryCode) {
        return this.categoryInfoMapper.deleteByCategoryCode(categoryCode);
    }

    @Override
    public void saveCategoryInfo(CategoryInfo bean) {
        //根据分类编号查询，因为数据库里给分类编号设置了唯一索引
        CategoryInfo dbBean = this.categoryInfoMapper.selectByCategoryCode(bean.getCategoryCode());
        //新增的特殊情况：新增的分类编号不能和数据库里的重复
        if (bean.getCategoryId() == null && dbBean != null) {
            throw new BusinessException("分类编号已经存在");
        }
        //修改的特殊情况：修改后的分类编号不能和数据库里的重复
        if (bean.getCategoryId() != null && dbBean != null && !bean.getCategoryId().equals(dbBean.getCategoryId())) {
            throw new BusinessException("分类编号已经存在");
        }
        if (bean.getCategoryId() == null) {
            //获取当前分类下(父级分类相同)的最大排序号
            Integer maxSort = this.categoryInfoMapper.selectMaxSort(bean.getpCategoryId());
            bean.setSort(maxSort + 1);
            //新增
            this.categoryInfoMapper.insert(bean);
        } else {
            //修改
            this.categoryInfoMapper.updateByCategoryId(bean, bean.getCategoryId());
        }
        //刷新缓存
        save2Redis();
    }

    @Override
    public void delCategory(Integer categoryId) {
        //TODO 判断该分类下是否有视频信息
        /*VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setCategoryIdOrPCategoryId(categoryId);
        Integer count = videoInfoService.findCountByParam(videoInfoQuery);
        if (count > 0) {
            throw new BusinessException("分类下有视频信息，无法删除");
        }*/

        CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
        categoryInfoQuery.setCategoryIdOrPCategoryId(categoryId);
        //根据分类id删除分类信息 以及 将其作为父级分类id删除该分类下的子分类
        categoryInfoMapper.deleteByParam(categoryInfoQuery);

        //刷新缓存
        save2Redis();
    }


    @Override
    public void changeSort(Integer pCategoryId, String categoryIds) {
        String[] categoryIdArray = categoryIds.split(",");
        List<CategoryInfo> categoryInfoList = new ArrayList<>();
        Integer sort = 1;
        for (String categoryId : categoryIdArray) {
            CategoryInfo categoryInfo = new CategoryInfo();
            categoryInfo.setCategoryId(Integer.parseInt(categoryId));
            categoryInfo.setPCategoryId(pCategoryId);
            categoryInfo.setSort(++sort);
            categoryInfoList.add(categoryInfo);
        }
        this.categoryInfoMapper.updateSortBatch(categoryInfoList);

        save2Redis();
    }

    //刷新缓存
    private void save2Redis() {
        CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
        categoryInfoQuery.setOrderBy("sort asc");
        List<CategoryInfo> sourceCategoryInfoList = this.categoryInfoMapper.selectList(categoryInfoQuery);
        List<CategoryInfo> categoryInfoList = convertLine2Tree(sourceCategoryInfoList, 0);
        redisComponent.saveCategoryList(categoryInfoList);
    }


    @Override
    public List<CategoryInfo> getAllCategoryList() {
        List<CategoryInfo> categoryInfoList = redisComponent.getCategoryList();
        if (categoryInfoList.isEmpty()) {
            save2Redis();
        }
        return redisComponent.getCategoryList();
    }
}
