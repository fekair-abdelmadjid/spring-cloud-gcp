/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gcp.autoconfigure.secretmanager;

import com.google.cloud.secretmanager.v1beta1.SecretVersionName;
import org.junit.Test;

import org.springframework.cloud.gcp.core.GcpProjectIdProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SecretManagerPropertySourceTests {

	private static final GcpProjectIdProvider DEFAULT_PROJECT_ID_PROVIDER = () -> "defaultProject";

	@Test
	public void testNonSecret() {
		String property = "spring.cloud.datasource";
		SecretVersionName secretIdentifier =
				SecretManagerPropertySource.parseFromProperty(property, DEFAULT_PROJECT_ID_PROVIDER);

		assertThat(secretIdentifier).isNull();
	}

	@Test
	public void testInvalidSecretFormat_missingSecretId() {
		String property = "gcp-secret/";

		assertThatThrownBy(() ->
				SecretManagerPropertySource.parseFromProperty(property, DEFAULT_PROJECT_ID_PROVIDER))
				.hasCauseInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("The GCP Secret Manager secret id must not be empty");
	}

	@Test
	public void testShortProperty_secretId() {
		String property = "gcp-secret/the-secret";
		SecretVersionName secretIdentifier =
				SecretManagerPropertySource.parseFromProperty(property, DEFAULT_PROJECT_ID_PROVIDER);

		assertThat(secretIdentifier.getProject()).isEqualTo("defaultProject");
		assertThat(secretIdentifier.getSecret()).isEqualTo("the-secret");
		assertThat(secretIdentifier.getSecretVersion()).isEqualTo("latest");
	}

	@Test
	public void testShortProperty_projectSecretId() {
		String property = "gcp-secret/the-secret/the-version";
		SecretVersionName secretIdentifier =
				SecretManagerPropertySource.parseFromProperty(property, DEFAULT_PROJECT_ID_PROVIDER);

		assertThat(secretIdentifier.getProject()).isEqualTo("defaultProject");
		assertThat(secretIdentifier.getSecret()).isEqualTo("the-secret");
		assertThat(secretIdentifier.getSecretVersion()).isEqualTo("the-version");
	}

	@Test
	public void testShortProperty_projectSecretIdVersion() {
		String property = "gcp-secret/my-project/the-secret/2";
		SecretVersionName secretIdentifier =
				SecretManagerPropertySource.parseFromProperty(property, DEFAULT_PROJECT_ID_PROVIDER);

		assertThat(secretIdentifier.getProject()).isEqualTo("my-project");
		assertThat(secretIdentifier.getSecret()).isEqualTo("the-secret");
		assertThat(secretIdentifier.getSecretVersion()).isEqualTo("2");
	}

	@Test
	public void testLongProperty_projectSecret() {
		String property = "gcp-secret/projects/my-project/secrets/the-secret";
		SecretVersionName secretIdentifier =
				SecretManagerPropertySource.parseFromProperty(property, DEFAULT_PROJECT_ID_PROVIDER);

		assertThat(secretIdentifier.getProject()).isEqualTo("my-project");
		assertThat(secretIdentifier.getSecret()).isEqualTo("the-secret");
		assertThat(secretIdentifier.getSecretVersion()).isEqualTo("latest");
	}

	@Test
	public void testLongProperty_projectSecretVersion() {
		String property = "gcp-secret/projects/my-project/secrets/the-secret/versions/3";
		SecretVersionName secretIdentifier =
				SecretManagerPropertySource.parseFromProperty(property, DEFAULT_PROJECT_ID_PROVIDER);

		assertThat(secretIdentifier.getProject()).isEqualTo("my-project");
		assertThat(secretIdentifier.getSecret()).isEqualTo("the-secret");
		assertThat(secretIdentifier.getSecretVersion()).isEqualTo("3");
	}
}