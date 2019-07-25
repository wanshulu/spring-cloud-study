package com.sgcc.cloud.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sgcc.cloud.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

}
