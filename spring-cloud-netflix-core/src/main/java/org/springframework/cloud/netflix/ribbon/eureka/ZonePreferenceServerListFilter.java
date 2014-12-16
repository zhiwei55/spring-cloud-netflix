/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.netflix.ribbon.eureka;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext.ContextKey;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAffinityServerListFilter;

/**
 * A filter that actively prefers the local zone (as defined by the deployment context, or
 * the Eureka instance metadata).
 * 
 * @author Dave Syer
 *
 * TODO: move out of ribbon.eureka package since it has nothing specific to eureka
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ZonePreferenceServerListFilter extends ZoneAffinityServerListFilter<Server> {

	private String zone;

	@Override
	public void initWithNiwsConfig(IClientConfig niwsClientConfig) {
		super.initWithNiwsConfig(niwsClientConfig);
		if (ConfigurationManager.getDeploymentContext() != null) {
			zone = ConfigurationManager.getDeploymentContext().getValue(ContextKey.zone);
		}
	}

	@Override
	public List<Server> getFilteredListOfServers(List<Server> servers) {
		List<Server> output = super.getFilteredListOfServers(servers);
		if (zone != null && output.size() == servers.size()) {
			List<Server> local = new ArrayList<Server>();
			for (Server server : output) {
				if (zone.equalsIgnoreCase(server.getZone())) {
					local.add(server);
				}
			}
			if (!local.isEmpty()) {
				return local;
			}
		}
		return output;
	}

}