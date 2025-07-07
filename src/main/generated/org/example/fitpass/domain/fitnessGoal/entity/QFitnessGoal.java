package org.example.fitpass.domain.fitnessGoal.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFitnessGoal is a Querydsl query type for FitnessGoal
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFitnessGoal extends EntityPathBase<FitnessGoal> {

    private static final long serialVersionUID = 1153069875L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFitnessGoal fitnessGoal = new QFitnessGoal("fitnessGoal");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> achievementDate = createDateTime("achievementDate", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Double> currentWeight = createNumber("currentWeight", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath description = createString("description");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final EnumPath<org.example.fitpass.domain.fitnessGoal.enums.GoalStatus> goalStatus = createEnum("goalStatus", org.example.fitpass.domain.fitnessGoal.enums.GoalStatus.class);

    public final EnumPath<org.example.fitpass.domain.fitnessGoal.enums.GoalType> goalType = createEnum("goalType", org.example.fitpass.domain.fitnessGoal.enums.GoalType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final NumberPath<Double> startWeight = createNumber("startWeight", Double.class);

    public final NumberPath<Double> targetWeight = createNumber("targetWeight", Double.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final org.example.fitpass.domain.user.entity.QUser user;

    public QFitnessGoal(String variable) {
        this(FitnessGoal.class, forVariable(variable), INITS);
    }

    public QFitnessGoal(Path<? extends FitnessGoal> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFitnessGoal(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFitnessGoal(PathMetadata metadata, PathInits inits) {
        this(FitnessGoal.class, metadata, inits);
    }

    public QFitnessGoal(Class<? extends FitnessGoal> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new org.example.fitpass.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

