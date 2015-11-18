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
package org.everit.authentication.simple;

import java.util.Objects;
import java.util.Optional;

import org.everit.authentication.simple.schema.qdsl.QSimpleSubject;
import org.everit.authenticator.Authenticator;
import org.everit.credential.encryptor.CredentialEncryptor;
import org.everit.credential.encryptor.CredentialMatcher;
import org.everit.persistence.querydsl.support.QuerydslSupport;
import org.everit.resource.resolver.ResourceIdResolver;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

/**
 * Implementation of {@link SimpleSubjectManager}, {@link Authenticator} and
 * {@link ResourceIdResolver}.
 */
public class AuthenticationSimpleImpl
    implements SimpleSubjectManager, Authenticator, ResourceIdResolver {

  private CredentialEncryptor credentialEncryptor;

  private CredentialMatcher credentialMatcher;

  private QuerydslSupport querydslSupport;

  /**
   * Constructor.
   *
   * @param credentialEncryptor
   *          the {@link CredentialEncryptor} instance.
   * @param credentialMatcher
   *          the {@link CredentialMatcher} instance.
   * @param querydslSupport
   *          the {@link QuerydslSupport} instance.
   *
   * @throws NullPointerException
   *           if one of the parameter is <code>null</code>.
   */
  public AuthenticationSimpleImpl(final CredentialEncryptor credentialEncryptor,
      final CredentialMatcher credentialMatcher, final QuerydslSupport querydslSupport) {
    this.credentialEncryptor =
        Objects.requireNonNull(credentialEncryptor, "credentialEncryptor cannot be null");
    this.credentialMatcher =
        Objects.requireNonNull(credentialMatcher, "credentialMatcher cannot be null");
    this.querydslSupport =
        Objects.requireNonNull(querydslSupport, "querydslSupport cannot be null");
  }

  @Override
  public Optional<String> authenticate(final String principal, final String credential) {

    if ((principal == null) || (credential == null)) {
      return Optional.empty();
    }

    String encryptedCredential = readEncryptedCredential(principal);
    if (encryptedCredential == null) {
      return Optional.empty();
    }

    boolean match = credentialMatcher.match(credential, encryptedCredential);
    if (match) {
      return Optional.of(principal);
    }

    return Optional.empty();
  }

  @Override
  public SimpleSubject create(final long resourceId, final String principal,
      final String plainCredential) {
    String encryptedCredential = (plainCredential == null) ? null
        : credentialEncryptor.encrypt(plainCredential);

    return querydslSupport.execute((connection, configuration) -> {
      QSimpleSubject qSimpleSubject = QSimpleSubject.simpleSubject;
      long simpleSubjectId = new SQLInsertClause(connection, configuration, qSimpleSubject)
          .set(qSimpleSubject.resourceId, resourceId)
          .set(qSimpleSubject.principal, principal)
          .set(qSimpleSubject.encryptedCredential, encryptedCredential)
          .executeWithKey(qSimpleSubject.simpleSubjectId);
      return new SimpleSubject(simpleSubjectId, principal, resourceId);
    });
  }

  @Override
  public boolean delete(final String principal) {
    long deleteCount = querydslSupport.execute((connection, configuration) -> {
      QSimpleSubject qSimpleSubject = QSimpleSubject.simpleSubject;
      return new SQLDeleteClause(connection, configuration, qSimpleSubject)
          .where(qSimpleSubject.principal.eq(principal))
          .execute();
    });
    return deleteCount > 0;
  }

  @Override
  public Optional<Long> getResourceId(final String principal) {
    return querydslSupport.execute((connection, configuration) -> {
      QSimpleSubject qSimpleSubject = QSimpleSubject.simpleSubject;
      Long resourceId = new SQLQuery<Long>(connection, configuration)
          .select(qSimpleSubject.resourceId)
          .from(qSimpleSubject)
          .where(qSimpleSubject.principal.eq(principal))
          .fetchFirst();
      return Optional.ofNullable(resourceId);
    });
  }

  @Override
  public String readEncryptedCredential(final String principal) {
    return querydslSupport.execute((connection, configuration) -> {
      QSimpleSubject qSimpleSubject = QSimpleSubject.simpleSubject;
      return new SQLQuery<String>(connection, configuration)
          .select(qSimpleSubject.encryptedCredential)
          .from(qSimpleSubject)
          .where(qSimpleSubject.principal.eq(principal))
          .fetchFirst();
    });
  }

  @Override
  public SimpleSubject readSimpleSubjectByPrincipal(final String principal) {
    return querydslSupport.execute((connection, configuration) -> {
      QSimpleSubject qSimpleSubject = QSimpleSubject.simpleSubject;
      return new SQLQuery<SimpleSubject>(connection, configuration)
          .select(Projections.constructor(SimpleSubject.class,
              qSimpleSubject.simpleSubjectId,
              qSimpleSubject.principal,
              qSimpleSubject.resourceId))
          .from(qSimpleSubject)
          .where(qSimpleSubject.principal.eq(principal))
          .fetchFirst();
    });
  }

  @Override
  public boolean updateCredential(final String principal, final String newPlainCredential) {
    String encryptedCredential = (newPlainCredential == null) ? null
        : credentialEncryptor.encrypt(newPlainCredential);

    long updateCount = querydslSupport.execute((connection, configuration) -> {
      QSimpleSubject qSimpleSubject = QSimpleSubject.simpleSubject;
      return new SQLUpdateClause(connection, configuration, qSimpleSubject)
          .where(qSimpleSubject.principal.eq(principal))
          .set(qSimpleSubject.encryptedCredential, encryptedCredential)
          .execute();
    });
    return updateCount > 0;
  }

  @Override
  public boolean updateCredential(final String principal, final String originalPlainCredential,
      final String newPlainCredential) {

    QSimpleSubject qSimpleSubject = QSimpleSubject.simpleSubject;

    String originalEncryptedCredential = readEncryptedCredential(principal);
    if ((originalEncryptedCredential != null)
        && !credentialMatcher.match(originalPlainCredential, originalEncryptedCredential)) {
      return false;
    }

    String encryptedNewCredential = (newPlainCredential == null) ? null
        : credentialEncryptor.encrypt(newPlainCredential);

    long updateCount = querydslSupport.execute((connection, configuration) -> {
      return new SQLUpdateClause(connection, configuration, qSimpleSubject)
          .where(qSimpleSubject.principal.eq(principal))
          .set(qSimpleSubject.encryptedCredential, encryptedNewCredential)
          .execute();
    });
    return updateCount > 0;
  }

  @Override
  public boolean updatePrincipal(final String principal, final String newPrincipal) {
    long updateCount = querydslSupport.execute((connection, configuration) -> {
      QSimpleSubject qSimpleSubject = QSimpleSubject.simpleSubject;
      return new SQLUpdateClause(connection, configuration, qSimpleSubject)
          .where(qSimpleSubject.principal.eq(principal))
          .set(qSimpleSubject.principal, newPrincipal)
          .execute();
    });
    return updateCount > 0;
  }

}
