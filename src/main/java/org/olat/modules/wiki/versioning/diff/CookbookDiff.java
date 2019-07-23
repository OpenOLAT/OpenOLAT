/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Adapted from <a href="http://javacook.darwinsys.com/">Ian Darwin's Java Cookbook</a>
 * See Diff.java for more information
 *
 * Result was printed, now the result is a list of ChangeInfo objects.
 * Input was from two files, now diff takes two Strings.
 * Changed to Java coding style.
 *
 * diff         Text file difference utility.
 * ----         Copyright 1987, 1989 by Donald C. Lindsay,
 *              School of Computer Science,  Carnegie Mellon University.
 *              Copyright 1982 by Symbionics.
 *              Use without fee is permitted when not for direct commercial
 *              advantage, and when credit to the source is given. Other uses
 *              require specific permission.
 *
 * Adaption Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
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

package org.olat.modules.wiki.versioning.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.modules.wiki.versioning.ChangeInfo;

/**
 * Returns differences between two text strings
 *
 * @author Stephan J. Schmidt
 *
 * @author	Ian F. Darwin, Java version
 * @author	D. C. Lindsay, C version (1982-1987)
 */

public class CookbookDiff {
  private ChangeInfo last;
  private List<String> lines = new ArrayList<>();

  /** block len > any possible real block len */
  private final int UNREAL = Integer.MAX_VALUE;

  /** Keeps track of information about file1 and file2 */
  private SourceInfo oldInfo, newInfo;

  /** blocklen is the info about found blocks. It will be set to 0, except
   * at the line#s where blocks start in the old file. At these places it
   * will be set to the # of lines in the block. During printout ,
   * this # will be reset to -1 if the block is printed as a MOVE block
   * (because the printout phase will encounter the block twice, but
   * must only print it once.)
   * The array declarations are to MAXLINECOUNT+2 so that we can have two
   * extra lines (pseudolines) at line# 0 and line# MAXLINECOUNT+1
   * (or less).
   */
  private int blocklen[];

  /** Do one string comparison. Called with both strings. */
  public List<ChangeInfo> diff(String oldText, String newText) {
  	Node.panchor = null;
    oldInfo = new SourceInfo();
    newInfo = new SourceInfo();
    /* we don't process until we know both files really do exist. */

    inputScan(oldText, oldInfo);
    inputScan(newText, newInfo);

    /* Now that we've read all the lines, allocate some arrays.
     */
    oldInfo.alloc();
    newInfo.alloc();

    blocklen = new int[(oldInfo.maxLine > newInfo.maxLine ?
      oldInfo.maxLine : newInfo.maxLine) + 2];

    /* Now do the work, and print the results. */
    transform();
    return printOut();
  }

  /**
   * inputscan    Reads the file specified by pinfo.file.
   * ---------    Places the lines of that file in the symbol table.
   *              Sets pinfo.maxLine to the number of lines found.
   */
  void inputScan(String input, SourceInfo pinfo) {
    pinfo.maxLine = 0;
    StringTokenizer tokenizer = new StringTokenizer(input, "\n\r");
    while (tokenizer.hasMoreTokens()) {
      String line = tokenizer.nextToken();
      storeLine(line, pinfo);
    }
  }

  /**
   * storeline    Places line into symbol table.
   * ---------    Expects pinfo.maxLine initted: increments.
   *              Places symbol table handle in pinfo.ymbol.
   *              Expects pinfo is either oldinfo or newinfo.
   */
  void storeLine(String linebuffer, SourceInfo pinfo) {
    int linenum = ++pinfo.maxLine;    /* note, no line zero */
    if (linenum > SourceInfo.MAXLINECOUNT) {
      System.err.println("MAXLINECOUNT exceeded, must stop.");
    }
    pinfo.symbol[linenum] =
      Node.addSymbol(linebuffer, pinfo == oldInfo, linenum);
  }

  /*
   * transform
   * Analyzes the file differences and leaves its findings in
   * the global arrays oldinfo.other, newinfo.other, and blocklen.
   * Expects both files in symtab.
   * Expects valid "maxLine" and "symbol" in oldinfo and newinfo.
   */
  void transform() {

    int oldline, newline;
    int oldmax = oldInfo.maxLine + 2;  /* Count pseudolines at  */
    int newmax = newInfo.maxLine + 2;  /* ..front and rear of file */

    for (oldline = 0; oldline < oldmax; oldline++)
      oldInfo.other[oldline] = -1;
    for (newline = 0; newline < newmax; newline++)
      newInfo.other[newline] = -1;

    scanUnique();  /* scan for lines used once in both files */
    scanAfter();   /* scan past sure-matches for non-unique blocks */
    scanBefore();  /* scan backwards from sure-matches */
    scanBlocks();  /* find the fronts and lengths of blocks */
  }

  /*
   * scanunique
   * Scans for lines which are used exactly once in each file.
   * Expects both files in symtab, and oldinfo and newinfo valid.
   * The appropriate "other" array entries are set to the line# in
   * the other file.
   * Claims pseudo-lines at 0 and XXXinfo.maxLine+1 are unique.
   */
  void scanUnique() {
    int oldline, newline;
    Node psymbol;

    for (newline = 1; newline <= newInfo.maxLine; newline++) {
      psymbol = newInfo.symbol[newline];
      if (psymbol.symbolIsUnique()) {        // 1 use in each file
        oldline = psymbol.linenum;
        newInfo.other[newline] = oldline; // record 1-1 map
        oldInfo.other[oldline] = newline;
      }
    }
    newInfo.other[0] = 0;
    oldInfo.other[0] = 0;
    newInfo.other[newInfo.maxLine + 1] = oldInfo.maxLine + 1;
    oldInfo.other[oldInfo.maxLine + 1] = newInfo.maxLine + 1;
  }

  /*
   * scanafter
   * Expects both files in symtab, and oldinfo and newinfo valid.
   * Expects the "other" arrays contain positive #s to indicate
   * lines that are unique in both files.
   * For each such pair of places, scans past in each file.
   * Contiguous groups of lines that match non-uniquely are
   * taken to be good-enough matches, and so marked in "other".
   * Assumes each other[0] is 0.
   */
  void scanAfter() {
    int oldline, newline;

    for (newline = 0; newline <= newInfo.maxLine; newline++) {
      oldline = newInfo.other[newline];
      if (oldline >= 0) {	/* is unique in old & new */
        for (; ;) {	/* scan after there in both files */
          if (++oldline > oldInfo.maxLine) break;
          if (oldInfo.other[oldline] >= 0) break;
          if (++newline > newInfo.maxLine) break;
          if (newInfo.other[newline] >= 0) break;

          /* oldline & newline exist, and
          aren't already matched */

          if (newInfo.symbol[newline] !=
            oldInfo.symbol[oldline])
            break;  // not same

          newInfo.other[newline] = oldline; // record a match
          oldInfo.other[oldline] = newline;
        }
      }
    }
  }

  /**
   * scanbefore
   * As scanafter, except scans towards file fronts.
   * Assumes the off-end lines have been marked as a match.
   */
  void scanBefore() {
    int oldline, newline;

    for (newline = newInfo.maxLine + 1; newline > 0; newline--) {
      oldline = newInfo.other[newline];
      if (oldline >= 0) {               /* unique in each */
        for (; ;) {
          if (--oldline <= 0) break;
          if (oldInfo.other[oldline] >= 0) break;
          if (--newline <= 0) break;
          if (newInfo.other[newline] >= 0) break;

          /* oldline and newline exist,
          and aren't marked yet */

          if (newInfo.symbol[newline] !=
            oldInfo.symbol[oldline])
            break;  // not same

          newInfo.other[newline] = oldline; // record a match
          oldInfo.other[oldline] = newline;
        }
      }
    }
  }

  /**
   * scanblocks - Finds the beginnings and lengths of blocks of matches.
   * Sets the blocklen array (see definition).
   * Expects oldinfo valid.
   */
  void scanBlocks() {
    int oldline, newline;
    int oldfront = 0;      // line# of front of a block in old, or 0
    int newlast = -1;      // newline's value during prev. iteration

    for (oldline = 1; oldline <= oldInfo.maxLine; oldline++)
      blocklen[oldline] = 0;
    blocklen[oldInfo.maxLine + 1] = UNREAL; // starts a mythical blk

    for (oldline = 1; oldline <= oldInfo.maxLine; oldline++) {
      newline = oldInfo.other[oldline];
      if (newline < 0)
        oldfront = 0;  /* no match: not in block */
      else {                                   /* match. */
        if (oldfront == 0) oldfront = oldline;
        if (newline != (newlast + 1)) oldfront = oldline;
        ++blocklen[oldfront];
      }
      newlast = newline;
    }
  }

  /* The following are global to printout's subsidiary routines */
  // enum{ idle, delete, insert, movenew, moveold,
  // same, change } printstatus;
  public static final int
    idle = 0, delete = 1, insert = 2, movenew = 3, moveold = 4,
  same = 5, change = 6;
  int printstatus;
  boolean anyprinted;
  int printoldline, printnewline;     // line numbers in old & new file

  /**
   * printout - Prints summary to stdout.
   * Expects all data structures have been filled out.
   */
  private List<ChangeInfo> printOut() {
    List<ChangeInfo> result = new ArrayList<>();

    printstatus = idle;
    anyprinted = false;
    for (printoldline = printnewline = 1; ;) {
      if (printoldline > oldInfo.maxLine) {
        newConsume(result);
        break;
      }
      if (printnewline > newInfo.maxLine) {
        oldConsume(result);
        break;
      }
      if (newInfo.other[printnewline] < 0) {
        if (oldInfo.other[printoldline] < 0)
          showChange(result);
        else
          showInsert(result);
      } else if (oldInfo.other[printoldline] < 0)
        showDelete(result);
      else if (blocklen[printoldline] < 0)
        skipOld();
      else if (oldInfo.other[printoldline] == printnewline)
        showSame();
      else
        showMove(result);
    }
    setLast(result);
    return result;
  }


  // Stores an info object
  // and adds all 'printed' lines to the
  // last one
  private void setLast(ChangeInfo info, List<ChangeInfo> result) {
    setLast(result);
    last = info;
  }

  private void setLast(List<ChangeInfo> result) {
    if (null != last) {
      last.setLines(lines.toArray(new String[0]));
      result.add(last);
    }
    lines = new ArrayList<>();
  }

  /*
   * newconsume        Part of printout. Have run out of old file.
   * Print the rest of the new file, as inserts and/or moves.
   */
  private void newConsume(List<ChangeInfo> result) {
    for (; ;) {
      if (printnewline > newInfo.maxLine)
        break;        /* end of file */
      if (newInfo.other[printnewline] < 0)
        showInsert(result);
      else 
        showMove(result);
    }
  }

  /**
   * oldconsume        Part of printout. Have run out of new file.
   * Process the rest of the old file, printing any
   * parts which were deletes or moves.
   */
  private void oldConsume(List<ChangeInfo> result) {
    for (; ;) {
      if (printoldline > oldInfo.maxLine)
        break;       /* end of file */
      printnewline = oldInfo.other[printoldline];
      if (printnewline < 0)
        showDelete(result);
      else if (blocklen[printoldline] < 0)
        skipOld();
      else
        showMove(result);
    }
  }

  /**
   * showdelete        Part of printout.
   * Expects printoldline is at a deletion.
   */
  private void showDelete(List<ChangeInfo> result) {
    if (printstatus != delete) {
      ChangeInfo info = new ChangeInfo(ChangeInfo.DELETE, printoldline, printoldline);
      setLast(info, result);
    }
    printstatus = delete;
    lines.add(oldInfo.symbol[printoldline].getSymbol());
    anyprinted = true;
    printoldline++;
  }

  /*
   * showinsert        Part of printout.
   * Expects printnewline is at an insertion.
   */
  private void showInsert(List<ChangeInfo> result) {
    if (printstatus == change) {
      // result.add(">>>>     CHANGED TO");
    } else if (printstatus != insert) {
      ChangeInfo info = new ChangeInfo(ChangeInfo.INSERT, printoldline, printoldline);
      setLast(info, result);
    }
    printstatus = insert;
    lines.add(newInfo.symbol[printnewline].getSymbol());
    anyprinted = true;
    printnewline++;
  }

  /**
   * showchange        Part of printout.
   * Expects printnewline is an insertion.
   *  Expects printoldline is a deletion.
   */
  private void showChange(List<ChangeInfo> result) {
    if (printstatus != change) {
      ChangeInfo info = new ChangeInfo(ChangeInfo.CHANGE, printoldline, printoldline);
      setLast(info, result);
    }
    printstatus = change;
    lines.add(oldInfo.symbol[printoldline].getSymbol());
    anyprinted = true;
    printoldline++;
  }

  /**
   * skipold           Part of printout.
   * Expects printoldline at start of an old block that has
   * already been announced as a move.
   * Skips over the old block.
   */
  private void skipOld() {
    printstatus = idle;
    for (; ;) {
      if (++printoldline > oldInfo.maxLine)
        break;     /* end of file  */
      if (oldInfo.other[printoldline] < 0)
        break;    /* end of block */
      if (blocklen[printoldline] != 0)
        break;          /* start of another */
    }
  }

  /**
   * skipnew           Part of printout.
   * Expects printnewline is at start of a new block that has
   * already been announced as a move.
   * Skips over the new block.
   */
  private void skipNew() {
    int oldline;
    printstatus = idle;
    for (; ;) {
      if (++printnewline > newInfo.maxLine)
        break;    /* end of file  */
      oldline = newInfo.other[printnewline];
      if (oldline < 0)
        break;                         /* end of block */
      if (blocklen[oldline] != 0)
        break;              /* start of another */
    }
  }

  /**
   * showsame          Part of printout.
   * Expects printnewline and printoldline at start of
   * two blocks that aren't to be displayed.
   */
  private void showSame() {
    int count;
    printstatus = idle;
    if (newInfo.other[printnewline] != printoldline) {
      System.err.println("BUG IN LINE REFERENCING");
    }
    count = blocklen[printoldline];
    printoldline += count;
    printnewline += count;
  }

  /**
   * showmove          Part of printout.
   * Expects printoldline, printnewline at start of
   * two different blocks ( a move was done).
   */
  private void showMove(List<ChangeInfo> result) {
    int oldblock = blocklen[printoldline];
    int newother = newInfo.other[printnewline];
    int newblock = blocklen[newother];

    if (newblock < 0)
      skipNew();         // already printed.
    else if (oldblock >= newblock) {     // assume new's blk moved.
      blocklen[newother] = -1;         // stamp block as "printed".
      ChangeInfo info = new ChangeInfo(ChangeInfo.MOVE, newother, printoldline);
      setLast(info, result);
      for (; newblock > 0; newblock--, printnewline++)
        lines.add(newInfo.symbol[printnewline].getSymbol());
      anyprinted = true;
      printstatus = idle;

    } else                /* assume old's block moved */
      skipOld();      /* target line# not known, display later */
  }
}

/**
 * Class "node". The symbol table routines in this class all
 * understand the symbol table format, which is a binary tree.
 * The methods are: addSymbol, symbolIsUnique, showSymbol.
 */
class Node {                       /* the tree is made up of these nodes */
  Node pleft, pright;
  int linenum;

  static final int freshnode = 0,
  oldonce = 1, newonce = 2, bothonce = 3, other = 4;

  int /* enum linestates */ linestate;
  String line;

  static Node panchor = null;    /* symtab is a tree hung from this */

  Node(String pline) {
    pleft = pright = null;
    linestate = freshnode;
    /* linenum field is not always valid */
    line = pline;
  }

  /**
   * matchsymbol       Searches tree for a match to the line.
   * If node's linestate == freshnode, then created the node.
   */
  static Node matchsymbol(String pline) {
    int comparison;
    Node pnode = panchor;
    if (panchor == null) return panchor = new Node(pline);
    for (; ;) {
      comparison = pnode.line.compareTo(pline);
      if (comparison == 0) return pnode;          /* found */

      if (comparison < 0) {
        if (pnode.pleft == null) {
          pnode.pleft = new Node(pline);
          return pnode.pleft;
        }
        pnode = pnode.pleft;
      }
      if (comparison > 0) {
        if (pnode.pright == null) {
          pnode.pright = new Node(pline);
          return pnode.pright;
        }
        pnode = pnode.pright;
      }
    }
    /* NOTE: There are return stmts, so control does not get here. */
  }

  /**
   * addSymbol(String pline) - Saves line into the symbol table.
   * Returns a handle to the symtab entry for that unique line.
   * If inoldfile nonzero, then linenum is remembered.
   */
  static Node addSymbol(String pline, boolean inoldfile, int linenum) {
    Node pnode;
    pnode = matchsymbol(pline);  /* find the node in the tree */
    if (pnode.linestate == freshnode) {
      pnode.linestate = inoldfile ? oldonce : newonce;
    } else {
      if ((pnode.linestate == oldonce && !inoldfile) ||
        (pnode.linestate == newonce && inoldfile))
        pnode.linestate = bothonce;
      else
        pnode.linestate = other;
    }
    if (inoldfile) pnode.linenum = linenum;
    return pnode;
  }

  /**
   * symbolIsUnique    Arg is a ptr previously returned by addSymbol.
   * --------------    Returns true if the line was added to the
   *                   symbol table exactly once with inoldfile true,
   *                   and exactly once with inoldfile false.
   */
  public boolean symbolIsUnique() {
    return (linestate == bothonce);
  }

  /**
   * showSymbol        Prints the line to stdout.
   */
  public String getSymbol() {
    return line;
  }
}

/** This is the info kept per-source.     */
class SourceInfo {
  static final int MAXLINECOUNT = 20000;

  public int maxLine;	/* After input done, # lines in file.  */
  Node symbol[]; /* The symtab handle of each line. */
  int other[]; /* Map of line# to line# in other file */
  /* ( -1 means don't-know ).            */
  /* Allocated AFTER the lines are read. */

  /**
   * Normal constructor
   */
  SourceInfo() {
    symbol = new Node[MAXLINECOUNT + 2];
    other = null;		// allocated later!
  }

  // This is done late, to be same size as # lines in input file.
  void alloc() {
    other = new int[symbol.length + 2];
  }
}