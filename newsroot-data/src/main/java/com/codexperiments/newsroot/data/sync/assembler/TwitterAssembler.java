package com.codexperiments.newsroot.data.sync.assembler;

import com.codexperiments.newsroot.api.entity.TweetDTO;
import com.codexperiments.newsroot.api.entity.UserDTO;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.entities.User;
import org.mapstruct.Mapper;

@Mapper
public interface TwitterAssembler {
    Tweet from(TweetDTO tweetDTO);

    User from(UserDTO userDTO);
}
