/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.authentication.simple.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSimpleSubject is a Querydsl query type for QSimpleSubject
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSimpleSubject extends com.querydsl.sql.RelationalPathBase<QSimpleSubject> {

    private static final long serialVersionUID = -719673281;

    public static final QSimpleSubject simpleSubject = new QSimpleSubject("authc_simple_subject");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QSimpleSubject> authcSimpleSubjectPk = createPrimaryKey(simpleSubjectId);

    }

    public final StringPath encryptedCredential = createString("encryptedCredential");

    public final StringPath principal = createString("principal");

    public final NumberPath<Long> resourceId = createNumber("resourceId", Long.class);

    public final NumberPath<Long> simpleSubjectId = createNumber("simpleSubjectId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public QSimpleSubject(String variable) {
        super(QSimpleSubject.class, forVariable(variable), "org.everit.authentication.simple", "authc_simple_subject");
        addMetadata();
    }

    public QSimpleSubject(String variable, String schema, String table) {
        super(QSimpleSubject.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSimpleSubject(Path<? extends QSimpleSubject> path) {
        super(path.getType(), path.getMetadata(), "org.everit.authentication.simple", "authc_simple_subject");
        addMetadata();
    }

    public QSimpleSubject(PathMetadata metadata) {
        super(QSimpleSubject.class, metadata, "org.everit.authentication.simple", "authc_simple_subject");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(encryptedCredential, ColumnMetadata.named("encrypted_credential").withIndex(3).ofType(Types.VARCHAR).withSize(256));
        addMetadata(principal, ColumnMetadata.named("principal").withIndex(2).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(resourceId, ColumnMetadata.named("resource_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(simpleSubjectId, ColumnMetadata.named("simple_subject_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

