/**
 * This file is part of org.everit.osgi.authentication.simple.
 *
 * org.everit.osgi.authentication.simple is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * org.everit.osgi.authentication.simple is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with org.everit.osgi.authentication.simple.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.authentication.simple.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.authentication.simple.CredentialEncryptor;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;

@Component(name = CredentialEncryptorConstants.COMPONENT_NAME, metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = CredentialEncryptorConstants.PROP_ALGORITHM,
                value = CredentialEncryptorConstants.DEFAULT_ALGORITHM)
})
@Service
public class CredentialEncryptorComponent implements CredentialEncryptor {

    private static final String PLAIN = "{plain}";

    private MessageDigest messageDigest;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentProperties)
            throws ConfigurationException {
        String algorithm = getStringProperty(componentProperties, CredentialEncryptorConstants.PROP_ALGORITHM);
        Hashtable<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put(CredentialEncryptorConstants.PROP_ALGORITHM, algorithm);
        try {
            messageDigest = MessageDigest.getInstance(algorithm); // TODO wire message digest like keystore component
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigurationException(
                    CredentialEncryptorConstants.PROP_ALGORITHM, "algorithm [" + algorithm + "] is not available", e);
        }
    }

    @Override
    public boolean checkCredential(final String plainCredential, final String encryptedCredential) {
        if (plainCredential == null) {
            throw new IllegalArgumentException("plainCredential cannot be null");
        }
        if (encryptedCredential == null) {
            throw new IllegalArgumentException("encryptedCredential cannot be null");
        }
        if (encryptedCredential.startsWith(PLAIN)) {
            return encryptedCredential.equals(PLAIN + plainCredential);
        }
        return encryptedCredential.equals(encryptCredential(plainCredential));
    }

    @Override
    public String encryptCredential(final String plainCredential) {
        if (plainCredential == null) {
            throw new IllegalArgumentException("plainCredential cannot be null");
        }
        byte[] bytesOfPlainCredential = StringUtils.getBytesUtf8(plainCredential);
        byte[] digest = messageDigest.digest(bytesOfPlainCredential);
        return format(digest);
    }

    private String format(final byte[] digest) {
        byte[] base64Digest = Base64.encodeBase64(digest);
        String base64DigestString = StringUtils.newStringUtf8(base64Digest);
        return base64DigestString;
    }

    private String getStringProperty(final Map<String, Object> componentProperties, final String propertyName)
            throws ConfigurationException {
        Object value = componentProperties.get(propertyName);
        if (value == null) {
            throw new ConfigurationException(propertyName, "property not defined");
        }
        return String.valueOf(value);
    }

}