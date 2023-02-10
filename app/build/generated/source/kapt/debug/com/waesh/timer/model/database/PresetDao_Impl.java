package com.waesh.timer.model.database;

import android.database.Cursor;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.waesh.timer.model.entity.TimerPreset;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class PresetDao_Impl implements PresetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TimerPreset> __insertionAdapterOfTimerPreset;

  private final EntityDeletionOrUpdateAdapter<TimerPreset> __deletionAdapterOfTimerPreset;

  private final EntityDeletionOrUpdateAdapter<TimerPreset> __updateAdapterOfTimerPreset;

  public PresetDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTimerPreset = new EntityInsertionAdapter<TimerPreset>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `preset` (`name`,`duration`,`ringTone_name`,`ringtone_uri`,`id`) VALUES (?,?,?,?,nullif(?, 0))";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TimerPreset value) {
        if (value.getName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getName());
        }
        stmt.bindLong(2, value.getDuration());
        if (value.getRingTone_name() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getRingTone_name());
        }
        if (value.getRingtone_uri() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getRingtone_uri());
        }
        stmt.bindLong(5, value.getId());
      }
    };
    this.__deletionAdapterOfTimerPreset = new EntityDeletionOrUpdateAdapter<TimerPreset>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `preset` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TimerPreset value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__updateAdapterOfTimerPreset = new EntityDeletionOrUpdateAdapter<TimerPreset>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `preset` SET `name` = ?,`duration` = ?,`ringTone_name` = ?,`ringtone_uri` = ?,`id` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TimerPreset value) {
        if (value.getName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getName());
        }
        stmt.bindLong(2, value.getDuration());
        if (value.getRingTone_name() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getRingTone_name());
        }
        if (value.getRingtone_uri() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getRingtone_uri());
        }
        stmt.bindLong(5, value.getId());
        stmt.bindLong(6, value.getId());
      }
    };
  }

  @Override
  public Object insertPreset(final TimerPreset preset,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTimerPreset.insert(preset);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deletePresets(final List<TimerPreset> presetList,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTimerPreset.handleMultiple(presetList);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object update(final TimerPreset preset, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTimerPreset.handle(preset);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<TimerPreset>> getAllPresets() {
    final String _sql = "SELECT `preset`.`name` AS `name`, `preset`.`duration` AS `duration`, `preset`.`ringTone_name` AS `ringTone_name`, `preset`.`ringtone_uri` AS `ringtone_uri`, `preset`.`id` AS `id` FROM preset";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"preset"}, new Callable<List<TimerPreset>>() {
      @Override
      public List<TimerPreset> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfName = 0;
          final int _cursorIndexOfDuration = 1;
          final int _cursorIndexOfRingToneName = 2;
          final int _cursorIndexOfRingtoneUri = 3;
          final int _cursorIndexOfId = 4;
          final List<TimerPreset> _result = new ArrayList<TimerPreset>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final TimerPreset _item;
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            final String _tmpRingTone_name;
            if (_cursor.isNull(_cursorIndexOfRingToneName)) {
              _tmpRingTone_name = null;
            } else {
              _tmpRingTone_name = _cursor.getString(_cursorIndexOfRingToneName);
            }
            final String _tmpRingtone_uri;
            if (_cursor.isNull(_cursorIndexOfRingtoneUri)) {
              _tmpRingtone_uri = null;
            } else {
              _tmpRingtone_uri = _cursor.getString(_cursorIndexOfRingtoneUri);
            }
            _item = new TimerPreset(_tmpName,_tmpDuration,_tmpRingTone_name,_tmpRingtone_uri);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
