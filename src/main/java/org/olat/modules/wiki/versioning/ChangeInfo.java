/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://snipsnap.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 */

package org.olat.modules.wiki.versioning;


/**
 * Stores diffs from one source to another
 *
 * @author Stephan J. Schmidt
 * @version $Id: ChangeInfo.java,v 1.2 2006-06-01 06:56:58 guretzki Exp $
 */

public class ChangeInfo {
  private int size;
  private int from;
  private int to;
  private String[] lines;
  private String type;
  public static final String MOVE="MOVE";
  public static final String DELETE="DELETE";
  public static final String INSERT="INSERT";
  public static final String CHANGE="CHANGE";

  public ChangeInfo(String type, int from, int to) {
    this.type = type;
    this.from = from;
    this.to = to;
  }

  public void setLines(String[] lines) {
    this.lines = lines;
    this.size = lines.length;
  }

  public int getSize() {
    return size;
  }

  public int getFrom() {
    return from;
  }

  public int getTo() {
    return to;
  }

  public String getType() {
    return type;
  }

  public String[] getLines() {
    return lines;
  }
}
