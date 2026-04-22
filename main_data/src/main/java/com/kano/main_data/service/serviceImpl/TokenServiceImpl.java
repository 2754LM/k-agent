package com.kano.main_data.service.serviceImpl;

import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {

    TokenCountEstimator tokenCountEstimator = new JTokkitTokenCountEstimator();

    @Override
    public int countTokens(String text) {
       return tokenCountEstimator.estimate(text);
    }

}
