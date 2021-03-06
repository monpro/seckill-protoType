package com.seckill.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.seckill.dao.UserDOMapper;
import com.seckill.dao.UserPasswordDOMapper;
import com.seckill.dataobject.UserDO;
import com.seckill.dataobject.UserPasswordDO;
import com.seckill.error.BusinessException;
import com.seckill.error.EnumError;
import com.seckill.service.OrderService;
import com.seckill.service.UserService;
import com.seckill.service.model.UserModel;
import com.seckill.validator.ValidationResult;
import com.seckill.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO == null){
            return null;
        }


        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return getUserModelFromDataObject(userDO, userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EnumError.PARAMETER_INVALIDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//                || StringUtils.isEmpty(userModel.getTelephone())
//                || userModel.getAge() == null
//                || userModel.getGender() == null){
//            throw new BusinessException(EnumError.PARAMETER_INVALIDATION_ERROR);
//        }

        ValidationResult result = validator.validate(userModel);

        if(result.isHasErrors()){
        throw new BusinessException(EnumError.PARAMETER_INVALIDATION_ERROR, result.getErrorMsg());
    }

        UserDO userDO = convertFromModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        }catch(DuplicateKeyException ex){
            throw new BusinessException(EnumError.PARAMETER_INVALIDATION_ERROR, "telephone number is existed");
        }
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel){

        if(userModel == null)
            return null;
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncryptPassword(userModel.getEncryptPassword());
        userPasswordDO.setUserId(userModel.getId());

        return userPasswordDO;
    }

    private UserDO convertFromModel(UserModel userModel){
        if(userModel == null)
            return null;
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);

        return userDO;
    }

    @Override
    public UserModel validateLogin(String telephone, String encryptPassword) throws BusinessException {
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO == null)
            throw new BusinessException(EnumError.USER_LOGIN_FAIL);

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = getUserModelFromDataObject(userDO, userPasswordDO);

        if(!StringUtils.equals(encryptPassword, userModel.getEncryptPassword()))
            throw new BusinessException(EnumError.USER_LOGIN_FAIL);
        return userModel;

    }

    private UserModel getUserModelFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if(userPasswordDO != null){
            userModel.setEncryptPassword(userPasswordDO.getEncryptPassword());
        }
        return userModel;
    }

}
