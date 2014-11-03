package com.codexperiments.newsroot.data.sync.assembler;

import com.codexperiments.newsroot.api.entity.TweetDTO;
import com.codexperiments.newsroot.api.entity.UserDTO;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.entities.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TwitterAssembler {
    Tweet from(TweetDTO tweetDTO);

    List<Tweet> from(List<TweetDTO> tweetDTO);

    User from(UserDTO userDTO);
}
