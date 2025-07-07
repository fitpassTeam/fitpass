package org.example.fitpass.domain.search.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSearchKeywordGym is a Querydsl query type for SearchKeywordGym
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchKeywordGym extends EntityPathBase<SearchKeywordGym> {

    private static final long serialVersionUID = 1381873519L;

    public static final QSearchKeywordGym searchKeywordGym = new QSearchKeywordGym("searchKeywordGym");

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

    public QSearchKeywordGym(String variable) {
        super(SearchKeywordGym.class, forVariable(variable));
    }

    public QSearchKeywordGym(Path<? extends SearchKeywordGym> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSearchKeywordGym(PathMetadata metadata) {
        super(SearchKeywordGym.class, metadata);
    }

}

