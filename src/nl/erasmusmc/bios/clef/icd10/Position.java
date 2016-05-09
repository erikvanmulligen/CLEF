package nl.erasmusmc.bios.clef.icd10;

public class Position {
    private Integer start = null;
    private Integer end = null;
    public Position(Integer aStartOffset, Integer aEndOffset) {
	this.start = aStartOffset;
	this.end = aEndOffset;
    }
    public Integer getStart() {
        return start;
    }
    public void setStart(Integer start) {
        this.start = start;
    }
    public Integer getEnd() {
        return end;
    }
    public void setEnd(Integer end) {
        this.end = end;
    }
}
