package org.example.fitpass.domain.search.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSearchKeywordTrainer is a Querydsl query type for SearchKeywordTrainer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchKeywordTrainer extends EntityPathBase<SearchKeywordTrainer> {

    private static final long serialVersionUID = -1743927191L;

    public static final QSearchKeywordTrainer searchKeywordTrainer = new QSearchKeywordTrainer("searchKeywordTrainer");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    public final NumberPath<Integer> count = createNumber("count", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSearchKeywordTrainer(String variable) {
        super(SearchKeywordTrainer.class, forVariable(variable));
    }

    public QSearchKeywordTrainer(Path<? extends SearchKeywordTrainer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSearchKeywordTrainer(PathMetadata metadata) {
        super(SearchKeywordTrainer.class, metadata);
    }

}

