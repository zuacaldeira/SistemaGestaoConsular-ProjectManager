import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-block-dialog',
  standalone: true,
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Bloquear Tarefa</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Razão do bloqueio</mat-label>
        <textarea matInput [(ngModel)]="reason" rows="3"
                  placeholder="Descreva a razão do bloqueio..."></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Cancelar</button>
      <button mat-raised-button color="warn" [disabled]="!reason.trim()" (click)="dialogRef.close(reason)">
        Confirmar Bloqueio
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; }
    mat-dialog-content { min-width: 400px; }
  `]
})
export class BlockDialogComponent {
  reason = '';

  constructor(public dialogRef: MatDialogRef<BlockDialogComponent>) {}
}
