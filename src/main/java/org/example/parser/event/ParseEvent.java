package org.example.parser.event;

import org.example.model.ErrorType;

public sealed interface ParseEvent permits
        ParseEvent.Progress,
        ParseEvent.Error,
        ParseEvent.TotalItems,
        ParseEvent.Completed,
        ParseEvent.Skipped,
        ParseEvent.Ignored {

    record Progress(double percent) implements ParseEvent {}
    record Error(ErrorType type, String message) implements ParseEvent {}
    record TotalItems(int total) implements ParseEvent {}
    record Completed() implements ParseEvent {}
    record Skipped() implements ParseEvent {}
    record Ignored() implements ParseEvent {}
}