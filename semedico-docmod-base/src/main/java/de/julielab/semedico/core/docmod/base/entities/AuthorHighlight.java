package de.julielab.semedico.core.docmod.base.entities;

public class AuthorHighlight extends Highlight {
    private float docscore;
    private String firstname;
    private String lastname;
    private String affiliation;
    private String firstnameForPrint;

    /**
     * Expects the highlight string to be a - potentially highlighted - author name of the form <tt>lastname, firstname</tt> or just <tt>name</tt>.
     * @param highlight An author name where the last and the first name may be separated by a comma.
     * @param field The name of the author field.
     * @param score The document hit score.
     */
    public AuthorHighlight(String highlight, String field, float score) {
        super(highlight, field, score);
        // TODO is it possible that the highlight is like "Smi<em>th, J</em>ohn?" This would break the eventual display when we just split at ','
        String[] names = highlight.split(",");
        if (names.length == 2) {
            setFirstname(names[1]);
            setLastname(names[0]);
        } else
            setLastname(highlight);
    }

    public float getDocscore() {
        return docscore;
    }

    public void setDocscore(float docscore) {
        this.docscore = docscore;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getFirstnameForPrint() {
        return firstnameForPrint;
    }

    public void setFirstnameForPrint(String firstnameForPrint) {
        this.firstnameForPrint = firstnameForPrint;
    }

    @Override
    public String toString() {
        if (null == firstnameForPrint) {
            setFirstnameForPrint();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(lastname).append(" ").append(firstnameForPrint);
        return sb.toString();
    }

    private void setFirstnameForPrint() {
        if (firstname == null) {
            return;
        }
        String[] split = firstname.split("\\s+");
        String withoutWS = "";
        boolean initialsOnly = true;

        for (String name : split) {
            if (name.length() > 1) {
                initialsOnly = false;
            }
            withoutWS += name;
        }
        if (initialsOnly) {
            firstnameForPrint = withoutWS;
        } else {
            firstnameForPrint = firstname;
        }
    }
}
