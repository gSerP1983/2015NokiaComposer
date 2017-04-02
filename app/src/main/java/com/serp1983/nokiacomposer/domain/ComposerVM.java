package com.serp1983.nokiacomposer.domain;


import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.view.View;
import android.widget.Button;

import com.serp1983.nokiacomposer.R;
import com.serp1983.nokiacomposer.util.DialogHelper;

import java.util.HashMap;
import java.util.Map;

public class ComposerVM extends RingtoneVM {
    public ObservableList<Note> Notes;
    public Note CurrentNote;
    private final Map<String, String> map = new HashMap<>();

    @Override
    public String getCode(){
        String res = "";
        for (Note note : Notes) {
            res += note.toString() + " ";
        }
        return res.trim();
    }

    @Override
    public void setCode(String code) {
        Notes.clear();
        for(String token : Note.getTokens(code)){
            Note note = new Note(token);
            Notes.add(note);
        }
        CurrentNote = Notes.get(Notes.size() - 1);
        super.setCode(getCode());
    }

    public ComposerVM(String name, int tempo) {
        super(name, tempo, "");

        Notes = new ObservableArrayList<>();
        map.put("0", "-");
        map.put("1", "C");
        map.put("2", "D");
        map.put("3", "E");
        map.put("4", "F");
        map.put("5", "G");
        map.put("6", "A");
        map.put("7", "B");
    }

    public void onDeleteClick() {
        if (CurrentNote != null){
            Notes.remove(CurrentNote);
        }
    }

    public void onClick(View v){
        String text = ((Button)v).getText().toString();

        // notes
        if (map.containsKey(text)) {
            Note token = new Note(getPrevDuration(), false, map.get(text), "0".equals(text) ? null : getPrevOctave());
            Notes.add(getIdx() + 1, token);
            CurrentNote = token;
        }

        if ("#".equals(text) && CurrentNote != null) {
            String note = CurrentNote.getNote();
            if (!"-".equals(note)) {
                if (note.length() == 2) {
                    CurrentNote.setNote(note.substring(1));
                    notifyChanged();
                } else {
                    if (!"E".equals(note) && !"B".equals(note)) {
                        CurrentNote.setNote("#" + note);
                        notifyChanged();
                    }
                }
            }
        }

        if ("*".equals(text) && CurrentNote != null){
            String note = CurrentNote.getNote();
            if (!"-".equals(note)){
                int octave = CurrentNote.getOctave();
                octave++;
                if (octave > 3)
                    octave = 1;
                CurrentNote.setOctave(octave);
                notifyChanged();
            }
        }

        if ("8".equals(text) && CurrentNote != null){
            int duration = CurrentNote.getDuration();
            duration *= 2;
            if (duration > 32)
                duration = 32;
            CurrentNote.setDuration(duration);
            notifyChanged();
        }

        if ("9".equals(text) && CurrentNote != null){
            int duration = CurrentNote.getDuration();
            duration /= 2;
            if (duration < 1)
                duration = 1;
            CurrentNote.setDuration(duration);
            notifyChanged();
        }
    }

    public boolean onLongClick(View v){
        String text = ((Button)v).getText().toString();
        if (!map.containsKey(text))
            return false;

        Note token = new Note(getPrevDuration(), true, map.get(text), "0".equals(text) ? null : getPrevOctave());
        Notes.add(getIdx() + 1, token);
        CurrentNote = token;

        return true;
    }

    public void onTempoClick(View v){
        Context context = v.getContext();
        String title = context.getString(R.string.ringtone_tempo_label);
        DialogHelper.showNumberPickerDialog(context, title, 10, 500, getTempo(), new DialogHelper.Callback<Integer>() {
            @Override
            public void onComplete(Integer input) {
                ComposerVM.this.setTempo(input);
            }
        });
    }

    private int getPrevDuration(){
        int idx = getIdx();
        if (idx < 0)
            return 4;

        for(int i = idx; i >= 0; i--){
            Note note = Notes.get(i);
            if (!note.getNote().equals("-"))
                return note.getDuration();
        }

        return 4;
    }

    private int getPrevOctave(){
        int idx = getIdx();
        if (idx < 0)
            return 1;

        for(int i = idx; i >= 0; i--){
            Note note = Notes.get(i);
            if (!note.getNote().equals("-"))
                return note.getOctave();
        }

        return 1;
    }

    private int getIdx(){
        for(int i = 0; i < Notes.size(); i++)
            if (Notes.get(i) == CurrentNote)
                return i;
        return -1;
    }

    private void notifyChanged(){
        int idx = getIdx();
        if (idx >= 0)
            Notes.set(idx, CurrentNote);
    }
}
