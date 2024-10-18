package com.practice.springbatch.batch;

import com.practice.springbatch.entity.WinEntity;
import com.practice.springbatch.repository.WinEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecondBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final WinEntityRepository winEntityRepository;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job secondJob() {

        return new JobBuilder("secondJob", jobRepository)
                .start(secondStep())
                .build();
    }

    @Bean
    public Step secondStep() {

        return new StepBuilder("secondStep", jobRepository)
                .<WinEntity, WinEntity> chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(winReader())
                .processor(trueProcessor())
                .writer(winWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<WinEntity> winReader() {

        return new RepositoryItemReaderBuilder<WinEntity>()
                .name("winReader")
                .pageSize(CHUNK_SIZE)
                .methodName("findByWinGreaterThanEqual")
                .arguments(Collections.singletonList(10L))
                .repository(winEntityRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<WinEntity, WinEntity> trueProcessor() {

        return item -> {
            item.setReward(true);

            return item;
        };
    }

    @Bean
    public RepositoryItemWriter<WinEntity> winWriter() {

        return new RepositoryItemWriterBuilder<WinEntity>()
                .repository(winEntityRepository)
                .methodName("save")
                .build();
    }

}
