package com.jxufe.mall.user.provider.services;

import com.alibaba.dubbo.common.utils.StringUtils;

import com.jxufe.mall.user.api.IUserCoreService;
import com.jxufe.mall.user.api.constants.Constants;
import com.jxufe.mall.user.api.constants.ResponseCodeEnum;
import com.jxufe.mall.user.api.dto.*;
import com.jxufe.mall.user.provider.dal.entity.User;
import com.jxufe.mall.user.provider.dal.persistence.UserMapper;
import com.jxufe.mall.user.provider.exception.ExceptionUtil;
import com.jxufe.mall.user.provider.exception.ServiceException;
import com.jxufe.mall.user.provider.exception.ValidateException;
import com.jxufe.mall.user.provider.utils.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 腾讯课堂搜索 咕泡学院
 * 加群获取视频：608583947
 * 风骚的Michael 老师
 */
@Service("userCoreService")
public class UserCoreServiceImpl implements IUserCoreService {

    Logger Log=LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserMapper userMapper;

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        Log.info("login request:"+request);
        UserLoginResponse response=new UserLoginResponse();
        try {
            beforeValidate(request);
            User user=userMapper.getUserByUserName(request.getUserName());
            if(user==null||!user.getPassword().equals(request.getPassword())){
                response.setCode(ResponseCodeEnum.USERORPASSWORD_ERRROR.getCode());
                response.setMsg(ResponseCodeEnum.USERORPASSWORD_ERRROR.getMsg());
                return response;
            }
            Map<String,Object> map=new HashMap<>();
            map.put("uid",user.getId());
            map.put("exp",DateTime.now().plusSeconds(40).toDate().getTime()/1000);

            response.setToken(JwtTokenUtils.generatorToken(map));

            response.setUid(user.getId());
            response.setAvatar(user.getAvatar());
            response.setCode(ResponseCodeEnum.SUCCESS.getCode());
            response.setMsg(ResponseCodeEnum.SUCCESS.getMsg());
        }catch (Exception e){
            Log.error("login occur exception :"+e);
            ServiceException serviceException=(ServiceException) ExceptionUtil.handlerException4biz(e);
            response.setCode(serviceException.getErrorCode());
            response.setMsg(serviceException.getErrorMessage());
        }finally {
            Log.info("login response->"+response);
        }

        return response;
    }

    @Override
    public CheckAuthResponse validToken(CheckAuthRequest request) {
        CheckAuthResponse response=new CheckAuthResponse();
        try{
            beforeValidateAuth(request);

            Claims claims=JwtTokenUtils.phaseToken(request.getToken());
            response.setUid(claims.get("uid").toString());
            response.setCode(ResponseCodeEnum.SUCCESS.getCode());
            response.setMsg(ResponseCodeEnum.SUCCESS.getMsg());

        }catch (ExpiredJwtException e){
            Log.error("Expire :"+e);
            response.setCode(ResponseCodeEnum.TOKEN_EXPIRE.getCode());
            response.setMsg(ResponseCodeEnum.TOKEN_EXPIRE.getMsg());
        }catch (SignatureException e1){
            Log.error("SignatureException :"+e1);
            response.setCode(ResponseCodeEnum.SIGNATURE_ERROR.getCode());
            response.setMsg(ResponseCodeEnum.SIGNATURE_ERROR.getMsg());
        }catch (Exception e){
            Log.error("login occur exception :"+e);
            ServiceException serviceException=(ServiceException) ExceptionUtil.handlerException4biz(e);
            response.setCode(serviceException.getErrorCode());
            response.setMsg(serviceException.getErrorMessage());
        }finally {
            Log.info("response:"+response);
        }

        return response;
    }

    public UserRegisterResponse register(UserRegisterRequest userRegisterRequest) {
        Log.info("begin UserCoreService.register,request:【"+userRegisterRequest+"】");

        UserRegisterResponse response=new UserRegisterResponse();
        try{
            beforeRegisterValidate(userRegisterRequest);

            User user=new User();
            user.setUsername(userRegisterRequest.getUsername());
            user.setPassword(userRegisterRequest.getPassword());
            user.setMobile(userRegisterRequest.getMobile());
            user.setSex(userRegisterRequest.getSex());
            user.setStatus(Constants.NORMAL_USER_STATUS);
            user.setCreateTime(new Date());

            int effectRow=userMapper.insertSelective(user);
            if(effectRow>0){
                response.setUid(user.getId());
                response.setCode(ResponseCodeEnum.SUCCESS.getCode());
                response.setMsg(ResponseCodeEnum.SUCCESS.getMsg());
                return  response;
            }
            response.setCode(ResponseCodeEnum.SYSTEM_BUSY.getCode());
            response.setMsg(ResponseCodeEnum.SYSTEM_BUSY.getMsg());
            return  response;
        }catch (DuplicateKeyException e){
            //TODO 用户名重复
        }catch(Exception e){
            e.printStackTrace();
            ServiceException serviceException=(ServiceException) ExceptionUtil.handlerException4biz(e);
            response.setCode(serviceException.getErrorCode());
            response.setMsg(serviceException.getErrorMessage());
        }finally {
            Log.info("register response:【"+response.toString()+"】");
        }

        return response;
    }

    private void beforeRegisterValidate(UserRegisterRequest request){
        if(null==request){
            throw new ValidateException("请求对象为空");
        }
        if(StringUtils.isEmpty(request.getUsername())){
            throw new ValidateException("用户名为空");
        }
        if(StringUtils.isEmpty(request.getPassword())){
            throw new ValidateException("密码为空");
        }
        if(StringUtils.isEmpty(request.getMobile())){
            throw new ValidateException("密码为空");
        }
    }

    private void beforeValidateAuth(CheckAuthRequest request){
        if(request==null){
            throw new ValidateException("请求对象为空");
        }
        if(StringUtils.isEmpty(request.getToken())){
            throw new ValidateException("token信息为空");
        }
    }


    private void beforeValidate(UserLoginRequest request){
        if(request==null){
            throw new ValidateException("请求对象为空");
        }
        if(StringUtils.isEmpty(request.getUserName())){
            throw new ValidateException("用户名为空");
        }
        if(StringUtils.isEmpty(request.getPassword())){
            throw new ValidateException("密码为空");
        }
    }
}
