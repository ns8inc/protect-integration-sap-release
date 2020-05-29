/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.ns8.hybris.fulfilmentprocess.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.ns8.hybris.fulfilmentprocess.constants.Ns8fulfilmentprocessConstants;

public class Ns8fulfilmentprocessManager extends GeneratedNs8fulfilmentprocessManager
{
	public static final Ns8fulfilmentprocessManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (Ns8fulfilmentprocessManager) em.getExtension(Ns8fulfilmentprocessConstants.EXTENSIONNAME);
	}
	
}
