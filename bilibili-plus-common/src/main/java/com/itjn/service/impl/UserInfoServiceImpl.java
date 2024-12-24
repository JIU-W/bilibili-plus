package com.itjn.service.impl;

import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.CountInfoDto;
import com.itjn.entity.dto.SysSettingDto;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.dto.UserCountInfoDto;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.enums.UserSexEnum;
import com.itjn.entity.enums.UserStatusEnum;
import com.itjn.entity.po.UserFocus;
import com.itjn.entity.po.UserInfo;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.SimplePage;
import com.itjn.entity.query.UserFocusQuery;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.exception.BusinessException;
import com.itjn.mappers.UserFocusMapper;
import com.itjn.mappers.UserInfoMapper;
import com.itjn.mappers.VideoInfoMapper;
import com.itjn.service.UserInfoService;
import com.itjn.utils.CopyTools;
import com.itjn.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询结果总记录数
     */
    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        //获取总记录数(查数据库得来的)
        int count = this.findCountByParam(param);

        //每页记录数(前端传过来的)
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        //当前页码(前端传过来的)
        int pageNo = param.getPageNo();

        //获取分页对象(分页对象包含了分页查询的详细信息)
        SimplePage page = new SimplePage(pageNo, count, pageSize);

        //将分页对象设置到param中
        param.setSimplePage(page);

        //结合前端传过来的页码和每页记录数以及分页对象中的start，end去进行最终的分页查询
        //(实际上SQL中用到的只有start和pageSize)(end等价于pageSize)
        List<UserInfo> list = this.findListByParam(param);

        PaginationResultVO<UserInfo> result = new PaginationResultVO(count, pageSize, pageNo, page.getPageTotal(), list);
        return result;                                                                        //总页数
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.deleteByParam(param);
    }

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email获取对象
     */
    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    /**
     * 根据NickName获取对象
     */
    @Override
    public UserInfo getUserInfoByNickName(String nickName) {
        return this.userInfoMapper.selectByNickName(nickName);
    }

    /**
     * 根据NickName修改
     */
    @Override
    public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
        return this.userInfoMapper.updateByNickName(bean, nickName);
    }

    /**
     * 根据NickName删除
     */
    @Override
    public Integer deleteUserInfoByNickName(String nickName) {
        return this.userInfoMapper.deleteByNickName(nickName);
    }


    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password) {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null != userInfo) {
            throw new BusinessException("邮箱账号已经存在");
        }
        UserInfo nickNameUser = this.userInfoMapper.selectByNickName(nickName);
        if (null != nickNameUser) {
            throw new BusinessException("昵称已经存在");
        }
        String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(StringTools.encodeByMD5(password));
        userInfo.setJoinTime(new Date());
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setSex(UserSexEnum.SECRECY.getType());
        userInfo.setTheme(Constants.ONE);

        // TODO
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        //初始硬币数量
        userInfo.setTotalCoinCount(sysSettingDto.getRegisterCoinCount());
        //当前硬币数量
        userInfo.setCurrentCoinCount(sysSettingDto.getRegisterCoinCount());

        this.userInfoMapper.insert(userInfo);
    }

    public TokenUserInfoDto login(String email, String password, String ip) {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或者密码错误");
        }
        if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastLoginTime(new Date());
        //设置最后一次登录ip地址
        updateInfo.setLastLoginIp(ip);
        //更新数据库的用户信息
        this.userInfoMapper.updateByUserId(updateInfo, userInfo.getUserId());
        TokenUserInfoDto tokenUserInfoDto = CopyTools.copy(userInfo, TokenUserInfoDto.class);
        //将登录用户的用户信息以及token信息保存到redis中
        redisComponent.saveTokenInfo(tokenUserInfoDto);
        return tokenUserInfoDto;
    }


    @Transactional
    public void updateUserInfo(UserInfo userInfo, TokenUserInfoDto tokenUserInfoDto) {
        //查询数据库用户信息
        UserInfo dbInfo = this.userInfoMapper.selectByUserId(userInfo.getUserId());
        if (!dbInfo.getNickName().equals(userInfo.getNickName()) && dbInfo.getCurrentCoinCount() < Constants.UPDATE_NICK_NAME_COIN) {
            throw new BusinessException("硬币不足，无法修改昵称");
        }
        if (!dbInfo.getNickName().equals(userInfo.getNickName())) {
            Integer count = this.userInfoMapper.updateCoinCountInfo(userInfo.getUserId(), -Constants.UPDATE_NICK_NAME_COIN);
            if (count == 0) {
                throw new BusinessException("硬币不足，无法修改昵称");
            }
        }
        //修改用户信息
        this.userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());

        Boolean updateTokenInfo = false;
        if (!userInfo.getAvatar().equals(tokenUserInfoDto.getAvatar())) {
            tokenUserInfoDto.setAvatar(userInfo.getAvatar());
            updateTokenInfo = true;
        }
        if (!tokenUserInfoDto.getNickName().equals(userInfo.getNickName())) {
            tokenUserInfoDto.setNickName(userInfo.getNickName());
            updateTokenInfo = true;
        }
        if (updateTokenInfo) {
            //更新token信息
            redisComponent.updateTokenInfo(tokenUserInfoDto);
        }
    }


    public UserInfo getUserDetailInfo(String currentUserId, String userId) {
        //查询用户信息
        UserInfo userInfo = getUserInfoByUserId(userId);
        if (null == userInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //查询用户的所有投稿总共的"播放数量、获赞数量"
        CountInfoDto countInfoDto = videoInfoMapper.selectSumCountInfo(userId);
        CopyTools.copyProperties(countInfoDto, userInfo);

        //查询用户的粉丝数、关注数
        Integer fansCount = userFocusMapper.selectFansCount(userId);
        Integer focusCount = userFocusMapper.selectFocusCount(userId);
        userInfo.setFansCount(fansCount);
        userInfo.setFocusCount(focusCount);

        if (currentUserId == null) {
            //当前没有用户登录：直接设置false
            userInfo.setHaveFocus(false);
        } else {
            //查询当前用户是否关注了该用户
            UserFocus userFocus = userFocusMapper.selectByUserIdAndFocusUserId(currentUserId, userId);
            //设置当前用户是否关注了该用户
            userInfo.setHaveFocus(userFocus == null ? false : true);
        }

        return userInfo;
    }


    public UserCountInfoDto getUserCountInfo(String userId) {
        UserInfo userInfo = getUserInfoByUserId(userId);
        Integer fansCount = userFocusMapper.selectFansCount(userId);
        Integer focusCount = userFocusMapper.selectFocusCount(userId);

        UserCountInfoDto countInfoDto = new UserCountInfoDto();

        countInfoDto.setFansCount(fansCount);
        countInfoDto.setFocusCount(focusCount);
        countInfoDto.setCurrentCoinCount(userInfo.getCurrentCoinCount());
        return countInfoDto;
    }

    public Integer updateCoinCountInfo(String userId, Integer changeCount) {
        if (changeCount < 0) {
            UserInfo userInfo = getUserInfoByUserId(userId);
            if (userInfo.getCurrentCoinCount() + changeCount < 0) {
                changeCount = -userInfo.getCurrentCoinCount();
            }
        }
        return this.userInfoMapper.updateCoinCountInfo(userId, changeCount);
    }

}
