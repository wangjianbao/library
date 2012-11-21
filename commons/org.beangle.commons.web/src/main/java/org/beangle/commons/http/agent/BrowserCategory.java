/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2012, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.http.agent;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.LinkedMap;
import org.beangle.commons.lang.Strings;

/**
 * Enum constants for most common browsers, including e-mail clients and bots.
 * 
 * @author harald
 */

public enum BrowserCategory {

  FIREFOX("Firefox", Engine.GECKO, "Firefox/(\\S*)->$1", "Firefox"),

  CHROME("Chrome", Engine.WEBKIT, "Chrome/(\\S*)->$1", "Chrome"),

  IE("Internet Explorer", Engine.TRIDENT, "MSIE (\\S*);->$1", "MSIE"),

  OPERA("Opera", Engine.PRESTO, "Opera/9.8->10", "Opera/9->9", "Opera"),

  OPERA_MINI("Opera Mini", Engine.PRESTO, "Opera Mini"),

  KONQUEROR("Konqueror", Engine.KHTML, "Konqueror"),

  OUTLOOK("Outlook", Engine.WORD, "MSOffice 12->2007", "MSOffice 14->2010", "MSOffice"),

  OUTLOOK_EXPRESS("Windows Live Mail", Engine.TRIDENT, "Outlook-Express/7.0->7.0"),

  IEMOBILE("IE Mobile", Engine.TRIDENT, "IEMobile 7->7", "IEMobile 6->6"),

  OMNIWEB("Omniweb", Engine.WEBKIT, "OmniWeb"), //

  SAFARI("Safari", Engine.WEBKIT, "Version/5->5", "Version/4->4", "Safari"),

  SAFARI_MOBILE("Mobile Safari", Engine.WEBKIT, "Mobile Safari", "Mobile/5A347 Safari",
      "Mobile/3A101a Safari", "Mobile/7B367 Safari"),

  APPLE_MAIL("Apple Mail", Engine.WEBKIT, "AppleWebKit"),

  LOTUS_NOTES("Lotus Notes", Engine.OTHER, "Lotus-Notes"),

  THUNDERBIRD("Thunderbird", Engine.GECKO, "Thunderbird/3->3", "Thunderbird/2->2", "Thunderbird"),

  CAMINO("Camino", Engine.GECKO, "Camino/2->2", "Camino"),

  FLOCK("Flock", Engine.GECKO, "Flock"),

  FIREFOX_MOBILE("Firefox Mobile", Engine.GECKO, "Firefox/3.5 Maemo->3"),

  SEAMONKEY("SeaMonkey", Engine.GECKO, "SeaMonkey"),

  BOT("Robot/Spider", Engine.OTHER, "Googlebot", "bot", "spider", "crawler", "Feedfetcher", "Slurp",
      "Twiceler", "Nutch", "BecomeBot"),

  MOZILLA("Mozilla", Engine.OTHER, "Mozilla", "Moozilla"),

  CFNETWORK("CFNetwork", Engine.OTHER, "CFNetwork"),

  EUDORA("Eudora", Engine.OTHER, "Eudora", "EUDORA"),

  POCOMAIL("PocoMail", Engine.OTHER, "PocoMail"),

  THEBAT("The Bat!", Engine.OTHER, "The Bat"),

  NETFRONT("NetFront", Engine.OTHER, "NetFront"),

  EVOLUTION("Evolution", Engine.OTHER, "CamelHttpStream"),

  LYNX("Lynx", Engine.OTHER, "Lynx"),

  DOWNLOAD("Downloading Tool", Engine.OTHER, "cURL", "wget"),

  UNKNOWN("Unknown", Engine.OTHER);

  private final String name;
  private final Engine engine;
  @SuppressWarnings("unchecked")
  private final Map<Pattern, String> versionMap = new LinkedMap();

  private BrowserCategory(String name, Engine renderEngine, String... versions) {
    this.name = name;
    this.engine = renderEngine;
    for (String version : versions) {
      String matcheTarget = version;
      String versionNum = "";
      if (Strings.contains(version, "->")) {
        matcheTarget = Strings.substringBefore(version, "->");
        versionNum = Strings.substringAfter(version, "->");
      }
      versionMap.put(Pattern.compile(matcheTarget), versionNum);
    }
  }

  public String getName() {
    return name;
  }

  /**
   * @return the rendering engine
   */
  public Engine getEngine() {
    return engine;
  }

  public String match(String agentString) {
    for (Map.Entry<Pattern, String> entry : versionMap.entrySet()) {
      Matcher m = entry.getKey().matcher(agentString);
      if (m.find()) {
        StringBuffer sb = new StringBuffer();
        m.appendReplacement(sb, entry.getValue());
        sb.delete(0, m.start());
        return sb.toString();
      }
    }
    return null;
  }
}
