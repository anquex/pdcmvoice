clc
clear all;
close all;
hold all;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% COMPUTE L_n with a given tree algorithm
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% i nodes -> L(i+1)

tic
nmax=1000;
p=0.5;
p=0.4175;
Q=zeros(nmax+1); % Q(i+1,n+1) <- Q_i(n)

for n=0:nmax
    for i=0:n
        Q(i+1,n+1)=binopdf(i,n,p);
    end
end

L=zeros(nmax+1,1);
L(1)=1;
L(2)=1;

for n=2:nmax
    s=0;
    for i=1:n-1
        s=s+Q(i+1,n+1)*( L(i+1)+L(n-i+1) );
    end
    %for Basic BT
    %L(n+1)=(1+Q(0+1,n+1)+Q(n+1,n+1)+s)/(1-Q(0+1,n+1)-Q(n+1,n+1));
    %for MBT
    L(n+1)=(1+Q(n+1,n+1)+s)/(1-Q(0+1,n+1)-Q(n+1,n+1));
end

toc
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
close all;
hold all;
batchsizes=[10,100,1000];
for t=1:length(batchsizes)
    n=batchsizes(t);
    n2=n/4;
    dn=2*n;
    v=n2:dn;
    if (length(v)>50)
        v=round(linspace(n2,dn,100));
    end
    M=zeros(1,length(v));
    k=0;
    for m=v;
        p=1/m;
        cost=0;
        for i=0:n
        cost=cost+L(i+1)*BINOPDF(i,n,p);
        end
        k=k+1;
        M(k)=cost*m;
    end
    h{t}=plot(n./v,M/L(n+1));
end


hx=xlabel('$\rho$','Interpreter','Latex');
hy=ylabel('$L''_{n}(\rho)/L_n$','Interpreter','Latex');
set(gca,'XDir','reverse');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 17);

legend('$n=10$','$n=100$','$n=1000$','Location','NorthWest');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 13);

grid on;
set(gca,'Ytick',[.8:0.025:1.05])

set(h{1}, 'LineWidth', 1.0);

%set(h{1}, 'LineStyle', '-', 'LineWidth', 1.0, 'Color', 'Black');
%set(h{1}, 'Marker', 'o', 'MarkerFaceColor', [0.5 0.5 0.5], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
%set(h{2}, 'LineStyle', '-', 'LineWidth', 1.0, 'Color',' Black');
%set(h{2}, 'Marker', 's', 'MarkerFaceColor', [1 1 1], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
%set(h{3}, 'LineStyle', '-', 'LineWidth', 1.0, 'Color',' Black');
%set(h{3}, 'Marker', 'd', 'MarkerFaceColor', [.8 .8 .8], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);

fh = figure(1); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white
saveas(fh, 'm-groups-MBT-ALOHA', 'epsc');
