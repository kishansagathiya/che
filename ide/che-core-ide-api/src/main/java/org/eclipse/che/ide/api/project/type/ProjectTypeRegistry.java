/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.project.type;

import java.util.List;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.commons.annotation.Nullable;

/** Registry of project types. */
public interface ProjectTypeRegistry {

  List<ProjectTypeDto> getProjectTypes();

  @Nullable
  ProjectTypeDto getProjectType(String id);
}
